package com.aueb.urbanarts;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
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
            final DocumentReference docRef = database.collection("users").document(mAuth.getCurrentUser().getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        final DocumentSnapshot document = task.getResult();
                        likedEvents = (Map<String, Boolean>) document.get("UserLiked");
                        final CollectionReference eventsCollection = database.collection("events");
                        eventsCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                                        } catch (Exception exception) {
                                            Log.d(TAG, "Exception");
                                        }
                                        docArtist = document.getString("Artist");
                                        docGenre = document.getString("genre");
                                        docLocation = document.getString("location");
                                        docGallery = (List<String>) document.get("gallery");
                                        try {
                                            docGalleryImage = docGallery.get(0);
                                        } catch (Exception e) {
                                            docGalleryImage = "none";
                                        }
                                        docLive = document.getBoolean("Live");
                                        docCommentCount = (List<String>) document.get("comments");
                                        docComments = docCommentCount.size();
                                        try {
                                            docLikes = Integer.parseInt(document.getString("likes"));
                                        } catch (Exception e) {
                                        }
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
                                        docArtistID = document.getString("ArtistID");
                                        if (!docArtistID.equals("")) {
                                            DocumentReference docRef = database.collection("artists").document(docArtistID);
                                            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                                    if (e != null) {
                                                        Log.w(TAG, "Listen failed.", e);
                                                        return;
                                                    }
                                                    if (documentSnapshot != null && documentSnapshot.exists()) {
                                                        docArtistProfilePicture = documentSnapshot.getString("profile_image_url");
                                                    }
                                                    recyclerView.setAdapter(adapter);
                                                    recyclerView.setLayoutManager(new LinearLayoutManager(Feed.this));
                                                    if (toBeAdded) {
                                                        mList.add(new item(docArtistProfilePicture, docGalleryImage, docArtist, docGenre, docLocation, docLive, docLikes, docComments, liked));
                                                        eventsList.add(document.getId());
                                                    }
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
                                                                        } else {
                                                                            Log.d(TAG, "No such document");
                                                                        }
                                                                    } else {
                                                                        Log.d(TAG, "get failed with ", task.getException());
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        } else {
                                            docArtistProfilePicture = "none";
                                            docArtist = "none";
                                            if (toBeAdded) {
                                                mList.add(new item(docArtistProfilePicture, docGalleryImage, docArtist, docGenre, docLocation, docLive, docLikes, docComments, liked));
                                                eventsList.add(document.getId());
                                            }
                                            recyclerView.setAdapter(adapter);
                                            recyclerView.setLayoutManager(new LinearLayoutManager(Feed.this));
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
                                                                } else {
                                                                    Log.d(TAG, "No such document");
                                                                }
                                                            } else {
                                                                Log.d(TAG, "get failed with ", task.getException());
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
                    }
                }
            });
        }

    }
}
