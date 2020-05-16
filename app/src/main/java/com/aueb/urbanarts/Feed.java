package com.aueb.urbanarts;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Feed extends AppCompatActivity {
    String location = "", name = "", typeOfArt = "";
    boolean live, locationExists = false, nameExists = false, typeOfArtExists = false;
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
        live = intent.getBooleanExtra("live", false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        List<item> mList = new ArrayList<>();

        mList.add(new item(R.drawable.artist_ex, "Ed Sheeran", "Dancing", "Athens", 5, 5, true));
        mList.add(new item(R.drawable.artist_image_1, "Ed Sheeran2", "Magic Show", "Athens", 15, 15, false));
        mList.add(new item(R.drawable.artist_image_2, "Ed Sheeran3", "Music", "Athens", 25, 25, true));
        mList.add(new item(R.drawable.artist_image_3, "Ed Sheeran4", "Painting", "Athens", 35, 35, true));
        mList.add(new item(R.drawable.artist_image_3, "Ed Dym", "Music", "Athens", 45, 45, false));
        mList.add(new item(R.drawable.artist_image_3, "Pan Ntym", "Stand-Up Comedy", "Athens", 55, 55, false));

        if (locationExists || nameExists || typeOfArtExists) {
            List<item> filterList = new ArrayList<>();
            adapter = new Adapter(this, filterList);
            size = mList.size();
            for (int i = 0; i < size; i++) {
                if (mList.get(i).getLiveEvent() == live) {
                    if (locationExists) {
                        if (nameExists) {
                            if (typeOfArtExists) {
                                if (mList.get(i).getLocation().equals(location) && mList.get(i).getArtistName().equals(name) && mList.get(i).getTypeOfArt().equals(typeOfArt)) {
                                    filterList.add(mList.get(i));
                                }
                            } else {
                                if (mList.get(i).getLocation().equals(location) && mList.get(i).getArtistName().equals(name)) {
                                    filterList.add(mList.get(i));
                                }
                            }
                        } else {
                            if (typeOfArtExists) {
                                if (mList.get(i).getLocation().equals(location) && mList.get(i).getTypeOfArt().equals(typeOfArt)) {
                                    filterList.add(mList.get(i));
                                }
                            } else {
                                if (mList.get(i).getLocation().equals(location)) {
                                    filterList.add(mList.get(i));
                                }
                            }
                        }
                    } else {
                        if (nameExists) {
                            if (typeOfArtExists) {
                                if (mList.get(i).getArtistName().equals(name) && mList.get(i).getTypeOfArt().equals(typeOfArt)) {
                                    filterList.add(mList.get(i));
                                }
                            } else {
                                if (mList.get(i).getArtistName().equals(name)) {
                                    filterList.add(mList.get(i));
                                }
                            }
                        } else {
                            if (mList.get(i).getTypeOfArt().equals(typeOfArt)) {
                                filterList.add(mList.get(i));
                            }
                        }
                    }
                }
            }
        } else {
            adapter = new Adapter(this, mList);
        }

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
