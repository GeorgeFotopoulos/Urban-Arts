package com.aueb.urbanarts;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class ShowMapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    GoogleMap map;
    LocationManager locationManager;
    protected EditText showAddress;
    double lat;
    double lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_map);
        checkPermission();
        showAddress = (EditText) findViewById(R.id.address_text);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);
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
        init();
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }

    }

    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        int height = 110;
        int width = 110;
        BitmapDrawable markerImage = (BitmapDrawable) getResources().getDrawable(R.drawable.marker_new);
        Bitmap b = markerImage.getBitmap();
        Bitmap smallerMarker = Bitmap.createScaledBitmap(b, width, height, false);


        lat = location.getLatitude();
        lon = location.getLongitude();
        LatLng position = new LatLng(lat, lon);
        locate();
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setMyLocationEnabled(false);
        map.addMarker(
                new MarkerOptions()
                        .position(position)
                        .icon(BitmapDescriptorFactory.fromBitmap(smallerMarker)));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15.0f));
    }

    protected void init() {
        showAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    locate();
                }
                return false;
            }
        });
    }

    protected void locate() {
        Geocoder geocoder = new Geocoder(ShowMapActivity.this);
        List<Address> addressList = new ArrayList<>();

        try {
            addressList = geocoder.getFromLocation(lat, lon, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!addressList.isEmpty()) {
            String address = (addressList.get(0)).getAddressLine(0);
            showAddress.setText(address);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider!" + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(ShowMapActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }
}
