package com.aueb.urbanarts;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Feed extends AppCompatActivity {
    String location = "", name = "", typeOfArt = "", liveStr = "";
    boolean locationExists = false, nameExists = false, typeOfArtExists = false;
    Boolean live;
    int size;
    Adapter adapter;

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

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        List<item> mList = new ArrayList<>();

        mList.add(new item(R.drawable.artist_ex, "Panos", "Dancing", "Piraeus", 5, 5, true));
        mList.add(new item(R.drawable.artist_image_1, "Panos", "Magic Show", "Piraeus", 15, 15, false));
        mList.add(new item(R.drawable.artist_image_2, "Panos", "Music", "piraeus", 25, 25, true));
        mList.add(new item(R.drawable.artist_image_3, "Panos", "Painting", "Athens", 35, 35, true));
        mList.add(new item(R.drawable.artist_ex, "panos", "Music", "Athens", 45, 45, false));
        mList.add(new item(R.drawable.artist_image_1, "panos", "Stand-Up Comedy", "Athens", 55, 55, false));
        mList.add(new item(R.drawable.artist_image_2, "panos", "Graffiti", "Athens", 55, 55, false));
        mList.add(new item(R.drawable.artist_image_3, "panos", "Graffiti", "Athens", 55, 55, true));
        mList.add(new item(R.drawable.artist_ex, "panos", "Magic Show", "Athens", 45, 45, false));
        mList.add(new item(R.drawable.artist_image_1, "panos", "Stand-Up Comedy", "piraeus", 55, 55, false));
        mList.add(new item(R.drawable.artist_image_2, "panos", "Dancing", "Athens", 55, 55, false));
        mList.add(new item(R.drawable.artist_image_3, "panos", "Stand-Up Comedy", "Piraeus", 55, 55, true));

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
    }
}
