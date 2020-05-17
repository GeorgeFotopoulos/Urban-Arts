package com.aueb.urbanarts;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Feed extends AppCompatActivity {
    String location = "", name = "", typeOfArt = "", liveStr = "", TAG = "", docArtist, docGenre, docLocation, docArtistID, docGalleryImage, docArtistProfilePicture;
    boolean locationExists = false, nameExists = false, typeOfArtExists = false, docLive;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    List<String> eventsList = new ArrayList<>();
    List<String> docGallery = new ArrayList<>();
    List<String> docCommentCount = new ArrayList<>();
    List<item> filterList = new ArrayList<>();
    int size, docLikes = 0, docComments = 0;
    List<item> mList = new ArrayList<>();
    Adapter adapter;
    Boolean live;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        final RecyclerView recyclerView = findViewById(R.id.recyclerView);

        final CollectionReference eventsCollection = database.collection("events");
        final CollectionReference artistsCollection = database.collection("artists");
        eventsCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        docArtist = document.getString("Artist");
                        docGenre = document.getString("genre");
                        docLocation = document.getString("location");
                        docGallery = (List<String>) document.get("gallery");
                        docGalleryImage = docGallery.get(0);
                        docLive = document.getBoolean("Live");
                        docCommentCount = (List<String>) document.get("comments");
                        docComments = docCommentCount.size();
                        docLikes = Integer.parseInt(document.getString("likes"));
                        docArtistID = document.getString("ArtistID");
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
                                mList.add(new item(docArtistProfilePicture, docGalleryImage, docArtist, docGenre, docLocation, docLive, docLikes, docComments));
                                adapter = new Adapter(Feed.this, mList);
                                recyclerView.setAdapter(adapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(Feed.this));
                            }
                        });
                    }
                    Log.d(TAG, eventsList.toString());
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        if (locationExists || nameExists || typeOfArtExists) {
            adapter = new Adapter(this, filterList);
            size = mList.size();
            for (int i = 0; i < size; i++) {
                if (locationExists) {
                    if (nameExists) {
                        if (typeOfArtExists) {
                            if (mList.get(i).getLocation().equalsIgnoreCase(location) && mList.get(i).getArtistName().equalsIgnoreCase(name) && mList.get(i).getTypeOfArt().equalsIgnoreCase(typeOfArt) && mList.get(i).isLiveEvent() == live) {
                                filterList.add(mList.get(i));
                            }
                        } else {
                            if (mList.get(i).getLocation().equalsIgnoreCase(location) && mList.get(i).getArtistName().equalsIgnoreCase(name) && mList.get(i).isLiveEvent() == live) {
                                filterList.add(mList.get(i));
                            }
                        }
                    } else {
                        if (typeOfArtExists) {
                            if (mList.get(i).getLocation().equalsIgnoreCase(location) && mList.get(i).getTypeOfArt().equalsIgnoreCase(typeOfArt) && mList.get(i).isLiveEvent() == live) {
                                filterList.add(mList.get(i));
                            }
                        } else {
                            if (mList.get(i).getLocation().equalsIgnoreCase(location) && mList.get(i).isLiveEvent() == live) {
                                filterList.add(mList.get(i));
                            }
                        }
                    }
                } else {
                    if (nameExists) {
                        if (typeOfArtExists) {
                            if (mList.get(i).getArtistName().equalsIgnoreCase(name) && mList.get(i).getTypeOfArt().equalsIgnoreCase(typeOfArt) && mList.get(i).isLiveEvent() == live) {
                                filterList.add(mList.get(i));
                            }
                        } else {
                            if (mList.get(i).getArtistName().equalsIgnoreCase(name) && mList.get(i).isLiveEvent() == live) {
                                filterList.add(mList.get(i));
                            }
                        }
                    } else {
                        if (mList.get(i).getTypeOfArt().equalsIgnoreCase(typeOfArt) && mList.get(i).isLiveEvent() == live) {
                            filterList.add(mList.get(i));
                        }
                    }
                }
            }
        } else if (live != null) {
            adapter = new Adapter(this, filterList);
            size = mList.size();
            for (int i = 0; i < size; i++) {
                if (mList.get(i).isLiveEvent() == live) {
                    filterList.add(mList.get(i));
                }
            }
        } else {
            adapter = new Adapter(this, mList);
            // Ίσως δημιουργήσει πρόβλημα
        }

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                final String artistName = mList.get(position).getArtistName();
                final String typeOfArt = mList.get(position).getTypeOfArt();
                final String eventLocation = mList.get(position).getLocation();

                DocumentReference docRef = database.collection("events").document("pz56iXB5RWdPygrRaoBT");
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                docArtist = document.getString("Artist");
                                docGenre = document.getString("genre");
                                docLocation = document.getString("location");
                                if (artistName.equalsIgnoreCase(docArtist) && typeOfArt.equalsIgnoreCase(docGenre) && eventLocation.equalsIgnoreCase(docLocation)) {
                                    docArtistID = document.getString("ArtistID");
                                }
                                Intent intent = new Intent(Feed.this, ArtistProfileActivity.class);
                                intent.putExtra("ARTIST_DOCUMENT_ID", docArtistID);
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
