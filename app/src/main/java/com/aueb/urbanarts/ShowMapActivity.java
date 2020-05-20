package com.aueb.urbanarts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class ShowMapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {
    GoogleMap map;
    Marker marker;
    LocationManager locationManager;
    GoogleApiClient mGoogleApiClient;
    Location currLocation;
    Geocoder geocoder;
    List<Address> addressList = new ArrayList<>();
    double distanceInMeters = 1000;
    double distanceInMap = 0.6;
    protected EditText showAddress;
    protected LatLng position;
    double lat;
    double lon;
    Circle circle;
    float zoomCamera = 14.2f;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<Marker> markersList = new ArrayList<>();
    ArrayList<String> eventsList = new ArrayList<>();
    ArrayList<String> currEventsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_map);
        checkPermission();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        getAllEvents();

        getLocation();
        showAddress = findViewById(R.id.address_text);
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

        findViewById(R.id.meters).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView text = findViewById(R.id.meters);
                deleteEvents();
                if (text.getText().toString().equals("500m")) {
                    text.setText("1km");
                    distanceInMeters = 1000;
                    distanceInMap = 0.6;
                    zoomCamera = 14.2f;

                } else if (text.getText().toString().equals("1km")) {
                    text.setText("1.5km");
                    distanceInMeters = 1500;
                    distanceInMap = 0.9;
                    zoomCamera = 13.8f;

                } else if (text.getText().toString().equals("1.5km")) {
                    text.setText("500m");
                    distanceInMeters = 500;
                    distanceInMap = 0.3;
                    zoomCamera = 14.8f;

                }
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, zoomCamera), new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        showEvents();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                createCircle(distanceInMeters);
            }
        });
    }

    private void getAllEvents() {
        db.collection("events")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                eventsList.add(document.getId());

                            }
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
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

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        map = googleMap;
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowClickListener(this);

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
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1, this);

        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        if (marker != null) {
            marker.remove();
        }

        deleteEvents();

        locate(location);

    }

    private void searchByName() {
        showAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {

                    onPause();

                    geocoder = new Geocoder(ShowMapActivity.this);
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
                                            .icon(BitmapDescriptorFactory.defaultMarker(343.0f)));
//                                            .icon(BitmapDescriptorFactory.fromBitmap(smallerMarker)));
                            deleteEvents();
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, zoomCamera), new GoogleMap.CancelableCallback() {
                                @Override
                                public void onFinish() {
                                    showEvents();
                                }

                                @Override
                                public void onCancel() {

                                }
                            });
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
    }

    private void locate(Location location) {

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

        geocoder = new Geocoder(ShowMapActivity.this);

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
                        .icon(BitmapDescriptorFactory.defaultMarker(343.0f)));
//                        .icon(BitmapDescriptorFactory.fromBitmap(smallerMarker)));

//        map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 14.8f));

        createCircle(distanceInMeters);
        deleteEvents();

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, zoomCamera), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                showEvents();
            }

            @Override
            public void onCancel() {

            }
        });


    }

    private void createCircle(double distanceInMeters) {
        if (circle != null) {
            circle.remove();
        }
//        java.util.List<PatternItem> pattern = Arrays.<PatternItem>asList(new Dot());
        circle = map.addCircle(new CircleOptions()
                .center(new LatLng(lat, lon))
                .strokeWidth(2.5f)
                .radius(distanceInMeters)
                .strokeColor(Color.BLACK)
                .fillColor(Color.TRANSPARENT));
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
        Toast.makeText(ShowMapActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(ShowMapActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent myIntent = new Intent(ShowMapActivity.this, HomePage.class);
        startActivity(myIntent);
        Animatoo.animateZoom(this);
        finish();
    }

    private void deleteEvents() {
        if (!markersList.isEmpty()) {
            for (int i = 0; i < markersList.size(); i++) {
                markersList.get(i).remove();
            }
            currEventsList.removeAll(currEventsList);
            markersList.removeAll(markersList);
        }
    }

    private void showEvents() {
        deleteEvents();
        String.valueOf(calculateDistance(lat, lon, 37.955250, 23.738130));
        int height = 100;
        int width = 80;
        BitmapDrawable markerImage = (BitmapDrawable) getResources().getDrawable(R.drawable.event_marker_green);
        markerImage.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#6600cc"), PorterDuff.Mode.MULTIPLY));
        Bitmap b = markerImage.getBitmap();
        Bitmap smallerMarker = Bitmap.createScaledBitmap(b, width, height, false);

        if (!eventsList.isEmpty()) {
            for (int i = 0; i < eventsList.size(); i++) {
                getEventLocation(eventsList.get(i), smallerMarker);
            }
        }
    }

    private void getEventLocation(final String event_id, final Bitmap smallerMarker) {
        DocumentReference docArtist = db.collection("events").document(event_id);
        docArtist.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        try {
                            List<Address> address = geocoder.getFromLocationName(String.valueOf(document.getString("location")), 1);

                            if (!address.isEmpty()) {
                                double eventLat = address.get(0).getLatitude();
                                double eventLon = address.get(0).getLongitude();

                                if (calculateDistance(lat, lon, eventLat, eventLon) < distanceInMap) {

                                    String artist = "";
                                    if (document.getString("Artist").equals("")) {
                                        artist = "Unknown Artist";
                                    } else {
                                        artist = document.getString("Artist");
                                    }

                                    Marker eventMarker = map.addMarker(
                                            new MarkerOptions()
                                                    .position(new LatLng(eventLat, eventLon))
                                                    .zIndex(1)
                                                    .title(document.getString("genre"))
                                                    .snippet(artist)
                                                    .icon(BitmapDescriptorFactory.fromBitmap(smallerMarker)));
                                    markersList.add(eventMarker);
                                    currEventsList.add(document.getId());
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker currMarker) {

        if (!String.valueOf(currMarker.getPosition()).equals(String.valueOf(marker.getPosition()))) {
            for (int i = 0; i < markersList.size(); i++) {
                if (String.valueOf(currMarker.getPosition()).equals(String.valueOf(markersList.get(i).getPosition())))
                    markersList.get(i).showInfoWindow();
            }
        }
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker currMarker) {
        for (int i = 0; i < markersList.size(); i++) {
            if ((String.valueOf(currMarker.getPosition())).equals(String.valueOf(markersList.get(i).getPosition()))) {
                goEvent(currEventsList.get(i));
            }
        }
    }

    private void goEvent(String eventID) {
        Intent intent = new Intent(ShowMapActivity.this, Event.class);
        intent.putExtra("eventID", eventID);
        startActivity(intent);
        Animatoo.animateFade(this);
        finish();
    }

}