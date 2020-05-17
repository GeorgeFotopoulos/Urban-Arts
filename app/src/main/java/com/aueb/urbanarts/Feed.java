package com.aueb.urbanarts;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

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
    String location = "", name = "", typeOfArt = "", liveStr = "", TAG = "", docArtist, docGenre, docLocation, docArtistID, docGalleryImage, docArtistProfilePicture;
    boolean locationExists = false, nameExists = false, typeOfArtExists = false, docLive,liked;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    List<String> eventsList = new ArrayList<>();
    List<String> docGallery = new ArrayList<>();
    List<String> docCommentCount = new ArrayList<>();
    List<item> filterList = new ArrayList<>();
    List<String> eventID = new ArrayList<>();
    int size, docLikes = 0, docComments = 0;
    List<item> mList = new ArrayList<>();
    Map<String, Boolean> likedEvents=new HashMap<>();
            ;
    private FirebaseAuth mAuth;
    Adapter adapter;
    Boolean live;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        final ImageView upvoteImage = findViewById(R.id.upvoteImage);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            final DocumentReference docRef = database.collection("users").document(mAuth.getCurrentUser().getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        final DocumentSnapshot document = task.getResult();
                        likedEvents = (Map<String, Boolean>) document.get("UserLiked");
                    }
                }
            });
        }

        final CollectionReference eventsCollection = database.collection("events");
        eventsCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (final QueryDocumentSnapshot document : task.getResult()) {
                        liked=false;
                        try {
                            Log.d("asdasda",document.getId());

                           if(likedEvents.get(document.getId())){
                                liked=true;
                           }
                        } catch (Exception exception) {
                            Log.d(TAG, "Mphke");
                            Log.d("asdasda________",document.getId());
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
                        docLikes = Integer.parseInt(document.getString("likes"));
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

                                    mList.add(new item(docArtistProfilePicture, docGalleryImage, docArtist, docGenre, docLocation, docLive, docLikes, docComments, liked));
                                    eventsList.add(document.getId());
                                    System.out.println("TEST");
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
                            mList.add(new item(docArtistProfilePicture, docGalleryImage, docArtist, docGenre, docLocation, docLive, docLikes, docComments,liked));
                            eventsList.add(document.getId());

                            Log.d("", eventsList.get(eventsList.size() - 1));
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

        //if (locationExists || nameExists || typeOfArtExists) {
        //    adapter = new Adapter(this, filterList);
        //    size = mList.size();
        //    for (int i = 0; i < size; i++) {
        //        if (locationExists) {
        //            if (nameExists) {
        //                if (typeOfArtExists) {
        //                    if (mList.get(i).getLocation().equalsIgnoreCase(location) && mList.get(i).getArtistName().equalsIgnoreCase(name) && mList.get(i).getTypeOfArt().equalsIgnoreCase(typeOfArt) && mList.get(i).isLiveEvent() == live) {
        //                        filterList.add(mList.get(i));
        //                    }
        //                } else {
        //                    if (mList.get(i).getLocation().equalsIgnoreCase(location) && mList.get(i).getArtistName().equalsIgnoreCase(name) && mList.get(i).isLiveEvent() == live) {
        //                        filterList.add(mList.get(i));
        //                    }
        //                }
        //            } else {
        //                if (typeOfArtExists) {
        //                    if (mList.get(i).getLocation().equalsIgnoreCase(location) && mList.get(i).getTypeOfArt().equalsIgnoreCase(typeOfArt) && mList.get(i).isLiveEvent() == live) {
        //                        filterList.add(mList.get(i));
        //                    }
        //                } else {
        //                    if (mList.get(i).getLocation().equalsIgnoreCase(location) && mList.get(i).isLiveEvent() == live) {
        //                        filterList.add(mList.get(i));
        //                    }
        //                }
        //            }
        //        } else {
        //            if (nameExists) {
        //                if (typeOfArtExists) {
        //                    if (mList.get(i).getArtistName().equalsIgnoreCase(name) && mList.get(i).getTypeOfArt().equalsIgnoreCase(typeOfArt) && mList.get(i).isLiveEvent() == live) {
        //                        filterList.add(mList.get(i));
        //                    }
        //                } else {
        //                    if (mList.get(i).getArtistName().equalsIgnoreCase(name) && mList.get(i).isLiveEvent() == live) {
        //                        filterList.add(mList.get(i));
        //                    }
        //                }
        //            } else {
        //                if (mList.get(i).getTypeOfArt().equalsIgnoreCase(typeOfArt) && mList.get(i).isLiveEvent() == live) {
        //                    filterList.add(mList.get(i));
        //                }
        //            }
        //        }
        //    }
        //} else if (live != null) {
        //    size = mList.size();
        //    for (int i = 0; i < size; i++) {
        //        if (mList.get(i).isLiveEvent() == live) {
        //            filterList.add(mList.get(i));
        //        }
        //    }
        //} else {
        //    adapter = new Adapter(this, mList);
        //    // Ίσως δημιουργήσει πρόβλημα
        //}
    }
}
