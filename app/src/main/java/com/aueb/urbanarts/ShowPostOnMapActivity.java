package com.aueb.urbanarts;

import android.Manifest;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("deprecation")
public class ShowPostOnMapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleMap map;
    Marker marker;
    LocationManager locationManager;
    GoogleApiClient mGoogleApiClient;
    Location currLocation;
    Geocoder geocoder;
    List<Address> addressList = new ArrayList<>();
    protected EditText showAddress;
    protected LatLng position;
    double lat;
    double lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_post_on_map);
        checkPermission();
        getLocation();
        showAddress = (EditText) findViewById(R.id.address_text);
        showAddress.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        showAddress.requestFocus();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        findViewById(R.id.find_me).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (map != null) {
                    onResume();
                    if (currLocation != null) locate(currLocation);
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15.0f));
                }
            }
        });

        showAddress.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showAddress.getText().clear();
            }
        });

        findViewById(R.id.post_button).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                confirmNewPost();
            }
        });
    }

    protected void confirmNewPost() {

        Dialog dialog = new Dialog(this, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.create_post_popup);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button yes = (Button) dialog.findViewById(R.id.yes);
        Button createnew = (Button) dialog.findViewById(R.id.createnew);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        createnew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        dialog.show();

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        currLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (currLocation != null) {
            lat = currLocation.getLatitude();
            lon = currLocation.getLongitude();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        getLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        boolean hideStores = googleMap.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.hide_stores)));
        map = googleMap;

        lat = 37.983810;
        lon = 23.727539;
        position = new LatLng(lat, lon);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 5.0f));

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            public void onMapClick(LatLng point) {
                onPause();
                Location newLocation = new Location("New Location");
                newLocation.setLatitude(point.latitude);
                newLocation.setLongitude(point.longitude);
                onLocationChanged(newLocation);
            }
        });

        searchByName();
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }

    }

    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 1, this);
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        if (marker != null) {
            marker.remove();
        }

        locate(location);

    }

    protected void searchByName() {
        showAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {

                    onPause();

                    geocoder = new Geocoder(ShowPostOnMapActivity.this);
                    try {

                        addressList = geocoder.getFromLocationName(String.valueOf(showAddress.getText()), 5);

                        if (!addressList.isEmpty()) {

                            int height = 120;
                            int width = 120;
                            BitmapDrawable markerImage = (BitmapDrawable) getResources().getDrawable(R.drawable.marker_new);
                            Bitmap b = markerImage.getBitmap();
                            Bitmap smallerMarker = Bitmap.createScaledBitmap(b, width, height, false);

                            if (marker != null) {
                                marker.remove();
                            }

                            for (int i = 0; i < addressList.size(); i++) {

                                Address correctAddress = addressList.get(i);

                                lon = correctAddress.getLongitude();
                                lat = correctAddress.getLatitude();

                                position = new LatLng(lat, lon);

                                showAddress.setText((addressList.get(i)).getAddressLine(0));
                                System.out.println((addressList.get(i)).getAddressLine(0));
                            }

                            marker = map.addMarker(
                                    new MarkerOptions()
                                            .position(position)
                                            .icon(BitmapDescriptorFactory.fromBitmap(smallerMarker)));
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15.0f));
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
    }

    protected void locate(Location location) {

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        int height = 120;
        int width = 120;
        BitmapDrawable markerImage = (BitmapDrawable) getResources().getDrawable(R.drawable.marker_new);
        Bitmap b = markerImage.getBitmap();
        Bitmap smallerMarker = Bitmap.createScaledBitmap(b, width, height, false);

        lat = location.getLatitude();
        lon = location.getLongitude();
        position = new LatLng(lat, lon);

        geocoder = new Geocoder(ShowPostOnMapActivity.this);

        try {
            addressList = geocoder.getFromLocation(lat, lon, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!addressList.isEmpty()) {
            String address = (addressList.get(0)).getAddressLine(0);
            showAddress.setText(address);
        }


        if (marker != null) {
            marker.remove();
        }

        marker = map.addMarker(
                new MarkerOptions()
                        .position(position)
                        .icon(BitmapDescriptorFactory.fromBitmap(smallerMarker)));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15.0f));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Provider Enabled! ",
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(ShowPostOnMapActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

