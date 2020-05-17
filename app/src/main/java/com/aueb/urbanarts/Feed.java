package com.aueb.urbanarts;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Feed extends AppCompatActivity {
    String location = "", name = "", typeOfArt = "", liveStr = "", TAG = "";
    String docArtist, docGenre, docLocation, docArtistID;
    boolean locationExists = false, nameExists = false, typeOfArtExists = false;
    Boolean live;
    int size;
    Adapter adapter;
    FirebaseFirestore database = FirebaseFirestore.getInstance();

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
        final List<item> mList = new ArrayList<>();

        mList.add(new item(R.drawable.artist_ex, "Panos", "Dancing", "Piraeus", true));
        mList.add(new item(R.drawable.artist_image_1, "Panos", "Magic Show", "Piraeus", false));
        mList.add(new item(R.drawable.artist_image_2, "Panos", "Music", "Piraeus", true));
        mList.add(new item(R.drawable.artist_image_3, "Panos", "Painting", "Athens", true));
        mList.add(new item(R.drawable.artist_ex, "Panos", "Music", "Athens", false));
        mList.add(new item(R.drawable.artist_image_1, "Panos", "Stand-Up Comedy", "Athens", false));
        mList.add(new item(R.drawable.artist_image_2, "Panos", "Graffiti", "Athens", false));
        mList.add(new item(R.drawable.artist_image_3, "Panos", "Graffiti", "Athens", true));
        mList.add(new item(R.drawable.artist_ex, "Panos", "Magic Show", "Athens", false));
        mList.add(new item(R.drawable.artist_image_1, "Panos", "Stand-Up Comedy", "Piraeus", false));
        mList.add(new item(R.drawable.artist_image_2, "Panos", "Dancing", "Athens", false));
        mList.add(new item(R.drawable.artist_image_3, "Professor Gooby", "Music", "Lesvou 8, Dafni, 17237, Athens, Greece", true));

        if (locationExists || nameExists || typeOfArtExists) {
            List<item> filterList = new ArrayList<>();
            adapter = new Adapter(this, filterList);
            size = mList.size();
            for (int i = 0; i < size; i++) {
                if (locationExists) {
                    if (nameExists) {
                        if (typeOfArtExists) {
                            if (mList.get(i).getLocation().equalsIgnoreCase(location) && mList.get(i).getArtistName().equalsIgnoreCase(name) && mList.get(i).getTypeOfArt().equalsIgnoreCase(typeOfArt) && mList.get(i).getLiveEvent() == live) {
                                filterList.add(mList.get(i));
                            }
                        } else {
                            if (mList.get(i).getLocation().equalsIgnoreCase(location) && mList.get(i).getArtistName().equalsIgnoreCase(name) && mList.get(i).getLiveEvent() == live) {
                                filterList.add(mList.get(i));
                            }
                        }
                    } else {
                        if (typeOfArtExists) {
                            if (mList.get(i).getLocation().equalsIgnoreCase(location) && mList.get(i).getTypeOfArt().equalsIgnoreCase(typeOfArt) && mList.get(i).getLiveEvent() == live) {
                                filterList.add(mList.get(i));
                            }
                        } else {
                            if (mList.get(i).getLocation().equalsIgnoreCase(location) && mList.get(i).getLiveEvent() == live) {
                                filterList.add(mList.get(i));
                            }
                        }
                    }
                } else {
                    if (nameExists) {
                        if (typeOfArtExists) {
                            if (mList.get(i).getArtistName().equalsIgnoreCase(name) && mList.get(i).getTypeOfArt().equalsIgnoreCase(typeOfArt) && mList.get(i).getLiveEvent() == live) {
                                filterList.add(mList.get(i));
                            }
                        } else {
                            if (mList.get(i).getArtistName().equalsIgnoreCase(name) && mList.get(i).getLiveEvent() == live) {
                                filterList.add(mList.get(i));
                            }
                        }
                    } else {
                        if (mList.get(i).getTypeOfArt().equalsIgnoreCase(typeOfArt) && mList.get(i).getLiveEvent() == live) {
                            filterList.add(mList.get(i));
                        }
                    }
                }
            }
        } else if (live != null) {
            List<item> filterList = new ArrayList<>();
            adapter = new Adapter(this, filterList);
            size = mList.size();
            for (int i = 0; i < size; i++) {
                if (mList.get(i).getLiveEvent() == live) {
                    filterList.add(mList.get(i));
                }
            }
        } else {
            adapter = new Adapter(this, mList);
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
                                if(artistName.equalsIgnoreCase(docArtist) && typeOfArt.equalsIgnoreCase(docGenre) && eventLocation.equalsIgnoreCase(docLocation)) {
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
