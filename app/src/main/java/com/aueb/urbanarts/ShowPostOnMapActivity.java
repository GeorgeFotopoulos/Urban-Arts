package com.aueb.urbanarts;


import android.Manifest;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class ShowPostOnMapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    Button b;
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    GoogleMap map;
    Marker marker;
    String url="";
    LocationManager locationManager;
    GoogleApiClient mGoogleApiClient;
    Location currLocation;
    Geocoder geocoder;
    List<Address> addressList = new ArrayList<>();
    protected EditText showAddress;
    protected LatLng position;
    double lat;
    double lon;
    boolean foundAnotherEvent = false;
    String name, typeOfArt, liveStr, filePathStr,comment="", ArtistID,postedBy;
    String anotherEventName, anotherEventType, anotherEventAddress;
    Boolean live;
    ArrayList<String> foundEvents = new ArrayList<>();
    Dialog currDialog;
    private FirebaseAuth mAuth;
    ArrayList<String> gallery=new ArrayList<>();
    ArrayList<String> Images=new ArrayList<>();
    String ID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_post_on_map);

        Intent intent = getIntent();
        if (intent.getStringExtra("name") != null) {
            name = intent.getStringExtra("name");
        }

        if (intent.getStringExtra("comment") != null) {
            comment = intent.getStringExtra("comment");
        }

        if (intent.getStringExtra("ID") != null) {
            ArtistID = intent.getStringExtra("ID");
        }

        if (intent.getStringExtra("postedBy") != null) {
            postedBy = intent.getStringExtra("postedBy");
        }

        if (intent.getStringExtra("typeOfArt") != null) {
            typeOfArt = intent.getStringExtra("typeOfArt");
        }

        if (intent.getStringExtra("live") != null) {
            liveStr = intent.getStringExtra("live");
            if (liveStr.equals("true")) {
                live = true;
            } else if (liveStr.equals("false")) {
                live = false;
            }
        }

        if (intent.getStringExtra("filePath") != null) {
            filePathStr = intent.getStringExtra("filePath");
        }

        checkPermission();
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

        findViewById(R.id.post_button).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                b=findViewById(R.id.post_button);
                b.setClickable(false);
                getEventDetails();

            }
        });
    }

    public void uploadImage() {
        try {
            if (!filePathStr.isEmpty()) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                ProgressDialog dialog = ProgressDialog.show(ShowPostOnMapActivity.this, "",
                        "Uploading photo and loading your post. Please wait...", true);
                // [START upload_create_reference]
                // Create a storage reference from our app
                StorageReference storageRef = storage.getReference();
                Uri file = Uri.parse(filePathStr);
                final StorageReference riversRef = storageRef.child("galleries/" + file.getLastPathSegment());
                UploadTask uploadTask = riversRef.putFile(file);

// Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Uri downloadUrl = uri;
                                url = downloadUrl.toString();
                                gallery.add(url);
                                DocumentReference washingtonRef = fStore.collection("events").document(ID);

// Set the "isCapital" field of the city 'DC'
                                washingtonRef
                                        .update("gallery", gallery)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                Intent myIntent = new Intent(ShowPostOnMapActivity.this, Event.class);
                                                myIntent.putExtra("eventID", ID);
                                                ShowPostOnMapActivity.this.startActivity(myIntent);
                                                finish();
                                                Log.d("TAG", "DocumentSnapshot successfully updated!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("TAG", "Error updating document", e);
                                            }
                                        });
                            }
                        });


                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                    }
                });
            }
        }catch(Exception e){
            Intent myIntent = new Intent(ShowPostOnMapActivity.this, Event.class);
            myIntent.putExtra("eventID", ID);
            ShowPostOnMapActivity.this.startActivity(myIntent);
            finish();

        }

    }

    protected void confirmNewPost() {
        if (currDialog != null) {
            currDialog.dismiss();
        }
        currDialog = new Dialog(this, android.R.style.Theme_Dialog);
        currDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        currDialog.setContentView(R.layout.create_post_popup);

        currDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                foundEvents.removeAll(foundEvents);
            }
        });

        TextView anEvAddressDisplay = currDialog.findViewById(R.id.address);
        TextView anEvGenreDisplay = currDialog.findViewById(R.id.genre);
        TextView anEvArtistDisplay = currDialog.findViewById(R.id.artist);

        anEvAddressDisplay.setText(anotherEventAddress);
        anEvGenreDisplay.setText(anotherEventType);
        anEvArtistDisplay.setText(anotherEventName);
        foundAnotherEvent = false;

        currDialog.setCanceledOnTouchOutside(true);
        currDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button yes = currDialog.findViewById(R.id.yes);
        Button createNew = currDialog.findViewById(R.id.create_new);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mergePost(foundEvents.get(foundEvents.size() - 1));
            }
        });

        createNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEventDetails();
            }
        });

        currDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        currDialog.getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        currDialog.show();
    }

    private void mergePost(final String s) {
        ID=s;
        b.setClickable(true);
        if (currDialog != null) {
            currDialog.dismiss();
        }
        DocumentReference docRef = fStore.collection("events").document(s);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("", "DocumentSnapshot data: " + document.getData());
                        ArrayList<String> comments=(ArrayList) document.get("comments");
                        if(!comment.isEmpty()) {
                            comments.add(postedBy + "@token@" + comment);
                            fStore.collection("events").document(s).update("comments", comments);
                        }
                        Images=(ArrayList) document.get("gallery");
                        gallery=Images;
                        uploadImage();



                    } else {
                        Log.d("", "No such document");
                    }
                } else {
                    Log.d("", "get failed with ", task.getException());
                }
            }
        });
//        Sixoneysi ton Events
    }

    private void makeNewPost() {
        b.setClickable(true);
        if (currDialog != null) {
            currDialog.dismiss();
        }
        //String name, typeOfArt, liveStr, filePathStr,comment, ArtistID;
        Map<String, Object> data = new HashMap<>();
        ArrayList<String> comments=new ArrayList<>();
        ArrayList<String> gallery=new ArrayList<>();
        if(!comment.isEmpty())
            comments.add(postedBy+"@token@"+comment);

        data.put("gallery",gallery);
        data.put("comments",comments);
        data.put("Artist",name );
        data.put("genre", typeOfArt);
        data.put("Live", live);
        data.put("likes", "0");
        data.put("ArtistID", ArtistID);
        data.put("location", showAddress.getText().toString());
        data.put("gallery",gallery);
        fStore.collection("events")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        ID=documentReference.getId();
                        uploadImage();
                        Log.d("", "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("", "Error adding document", e);
                    }
                });


//        Edo kane dhmiourgia tou event
    }

    private void getEventDetails() {
        fStore.collection("events")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                double currEventLat = getLat(document.getString("location"));
                                double currEventLon = getLon(document.getString("location"));
                                if (calculateDistance(currEventLat, currEventLon, lat, lon) < 0.05 && document.getString("genre").equals(typeOfArt) && !foundEvents.contains(document.getId())) {
                                    foundAnotherEvent = true;
                                    anotherEventAddress = document.getString("location");
                                    anotherEventType = document.getString("genre");
                                    if (document.getString("Artist").equals("")) {
                                        anotherEventName = "Unknown Artist";
                                    } else {
                                        anotherEventName = document.getString("Artist");
                                    }
                                    foundEvents.add(document.getId());
                                    break;
                                }
                            }
                            if (foundAnotherEvent) {
                                confirmNewPost();
                            } else {
                                makeNewPost();
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });
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
                                            .icon(BitmapDescriptorFactory.defaultMarker(343.0f)));
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
                        .icon(BitmapDescriptorFactory.defaultMarker(343.0f)));
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

    private double getLat(String addresName) {
        geocoder = new Geocoder(ShowPostOnMapActivity.this);
        try {

            addressList = geocoder.getFromLocationName(addresName, 1);

            if (!addressList.isEmpty()) {
                double currlat = 0;
                for (int i = 0; i < addressList.size(); i++) {

                    Address correctAddress = addressList.get(i);

                    currlat = correctAddress.getLatitude();

                }
                return currlat;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        return 0;
    }

    private double getLon(String addresName) {
        geocoder = new Geocoder(ShowPostOnMapActivity.this);
        try {

            addressList = geocoder.getFromLocationName(addresName, 1);

            if (!addressList.isEmpty()) {
                double currlon = 0;
                for (int i = 0; i < addressList.size(); i++) {

                    Address correctAddress = addressList.get(i);

                    currlon = correctAddress.getLongitude();

                }
                return currlon;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        return 0;
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
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ShowPostOnMapActivity.this, HomePage.class);
        startActivity(intent);
        Animatoo.animateZoom(this);
        finish();
    }
}