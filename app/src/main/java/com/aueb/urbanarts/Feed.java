package com.aueb.urbanarts;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Feed extends AppCompatActivity {
    String docArtist, docGenre, docLocation, docArtistID, docGalleryImage, docArtistProfilePicture;
    String location = "", name = "", typeOfArt = "", liveStr = "", TAG = "";
    boolean locationExists = false, nameExists = false, typeOfArtExists = false;
    boolean docLive, liked, toBeAdded = true;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    Map<String, Boolean> likedEvents = new HashMap<>();
    List<String> docCommentCount = new ArrayList<>();
    List<String> eventsList = new ArrayList<>();
    List<String> docGallery = new ArrayList<>();
    List<item> mList = new ArrayList<>();
    int docLikes = 0, docComments = 0;
    private FirebaseAuth mAuth;
    Adapter adapter;
    Boolean live;
    ArrayList<String> artistsList = new ArrayList<>();
    ArrayList<String> artistsImages = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        adapter = new Adapter(Feed.this, mList);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Intent intent = getIntent();
        if (intent.getStringExtra("location") != null) {
            location = intent.getStringExtra("location");
            locationExists = true;
        }

        if (intent.getStringExtra("name") != null) {
            name = intent.getStringExtra("name");
            nameExists = true;
        }
        if (intent.getStringExtra("typeOfArt") != null) {
            typeOfArt = intent.getStringExtra("typeOfArt");
            typeOfArtExists = true;
        }

        if (intent.getStringExtra("live") != null) {
            liveStr = intent.getStringExtra("live");
            if (liveStr.equals("true")) {
                live = true;
            } else if (liveStr.equals("false")) {
                live = false;
            }
        }


        ImageButton btn_map = findViewById(R.id.my_location);
        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Feed.this, ShowMapActivity.class);
                intent.putStringArrayListExtra("events", (ArrayList<String>) eventsList);
                startActivity(intent);
                finish();
            }
        });

        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                final String eventID = eventsList.get(position);
                DocumentReference docRef = database.collection("events").document(eventID);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Intent intent = new Intent(Feed.this, Event.class);
                                intent.putExtra("eventID", eventID);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
                });
            }
        });

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {

                            mAuth = FirebaseAuth.getInstance();
                            if (mAuth.getCurrentUser() != null) {
                                final DocumentReference userLikedReference = database.collection("users").document(mAuth.getCurrentUser().getUid());
                                userLikedReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            final DocumentSnapshot document = task.getResult();
                                            try {
                                                likedEvents = (Map<String, Boolean>) document.get("UserLiked");
                                            } catch (Exception ignore) {

                                            }
                                        }
                                    }
                                });
                            }

                            final RecyclerView recyclerView = findViewById(R.id.recyclerView);

                            double lat = location.getLatitude();
                            double lng = location.getLongitude();

                            makeArtists(recyclerView, lat, lng);


                        }
                    }
                });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Feed.this, HomePage.class);
        startActivity(intent);
        Animatoo.animateZoom(this);
        finish();
    }

    public void makeArtists(final RecyclerView recyclerView, final double lat, final double lng) {
        database.collection("artists").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        artistsList.add(document.getId());
                        artistsImages.add(document.getString("profile_image_url"));
                    }
                    connectThem(recyclerView, lat, lng);
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }

    private void connectThem(final RecyclerView recyclerView, final double lat, final double lng) {
        CollectionReference eventsReference = database.collection("events");
        eventsReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (final QueryDocumentSnapshot document : task.getResult()) {
                        toBeAdded = true;
                        liked = false;

                        Geocoder geocoder = new Geocoder(Feed.this);
                        try {
                            List<Address> address = geocoder.getFromLocationName(document.getString("location"), 1);

                            Address correctAddress = address.get(0);
                            double eventLat = correctAddress.getLatitude();
                            double eventLng = correctAddress.getLongitude();


                            try {
                                if (likedEvents.get(document.getId())) {
                                    liked = true;
                                }
                            } catch (Exception ignore) {

                            }
                            docArtist = document.getString("Artist");
                            docArtistID = document.getString("ArtistID");

                            docLive = document.getBoolean("Live");
                            docCommentCount = (List<String>) document.get("comments");
                            docComments = docCommentCount.size();
                            try {
                                docLikes = Integer.parseInt(document.getString("likes"));
                            } catch (Exception ignore) {

                            }
                            docGallery = (List<String>) document.get("gallery");
                            try {
                                docGalleryImage = docGallery.get(0);
                            } catch (Exception e) {
                                docGalleryImage = "none";
                            }
                            docGenre = document.getString("genre");
                            docLocation = document.getString("location");

                            if (locationExists) {
                                if (!location.equals(docLocation))
                                    toBeAdded = false;
                            }
                            if (nameExists) {
                                if (!name.equals(docArtist))
                                    toBeAdded = false;
                            }
                            if (typeOfArtExists) {
                                if (!typeOfArt.equals(docGenre))
                                    toBeAdded = false;
                            }
                            if (live != null) {
                                if (!(live == docLive))
                                    toBeAdded = false;
                            }

                            if (calculateDistance(eventLat, eventLng, lat, lng) < 5) {

                                if (artistsList.contains(docArtistID)) {
                                    for (int i = 0; i < artistsList.size(); i++) {
                                        if (artistsList.get(i).equals(docArtistID)) {
                                            addItem(artistsImages.get(i), docArtist, document, recyclerView);
                                            break;
                                        }
                                    }
                                } else {
                                    if (docArtist.equals("")) {
                                        addItem("none", "none", document, recyclerView);
                                    } else {
                                        addItem("none", docArtist, document, recyclerView);
                                    }
                                }
                                recyclerView.setAdapter(adapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(Feed.this));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }
        });
    }

    public void addItem(String docArtistProfilePicture, String docArtist, QueryDocumentSnapshot document, RecyclerView recyclerView) {
        try {
            if (toBeAdded) {
                mList.add(new item(docArtistProfilePicture, docGalleryImage, docArtist, docGenre, docLocation, docLive, docLikes, docComments, liked));

                eventsList.add(document.getId());
            }
        } catch (Exception ignore) {

        }
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

}