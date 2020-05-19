package com.aueb.urbanarts;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        adapter = new Adapter(Feed.this, mList);

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

        final RecyclerView recyclerView = findViewById(R.id.recyclerView);

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

        final CollectionReference eventsReference = database.collection("events");
        eventsReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (final QueryDocumentSnapshot document : task.getResult()) {
                        toBeAdded = true;
                        liked = false;
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

                        if (docArtistID.equals("")) docArtistID = "na";
                        DocumentReference docRef = database.collection("artists").document(docArtistID);
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    try {
                                        if (documentSnapshot.exists()) {
                                            docArtistProfilePicture = documentSnapshot.getString("profile_image_url");
                                            addItem(docArtistProfilePicture, docArtist, document, recyclerView);
                                        } else {
                                            docArtistProfilePicture = "none";
                                            docArtist = "none";
                                            addItem(docArtistProfilePicture, docArtist, document, recyclerView);
                                        }
                                    } catch (Exception ignore) {

                                    }
                                }
                            }
                        });
                    }

                }
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
    }

    public void addItem(String docArtistProfilePicture, String docArtist, QueryDocumentSnapshot document, RecyclerView recyclerView) {
        try {
            if (toBeAdded) {
                mList.add(new item(docArtistProfilePicture, docGalleryImage, docArtist, docGenre, docLocation, docLive, docLikes, docComments, liked));
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(Feed.this));
                eventsList.add(document.getId());
            }
        } catch (Exception ignore) {

        }
    }

}