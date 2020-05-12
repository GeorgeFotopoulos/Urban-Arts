package com.aueb.urbanarts;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Feed extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        List<item> mList = new ArrayList<>();
        Adapter adapter = new Adapter(this, mList);
        mList.add(new item(R.drawable.artist_ex, "Ed Sheeran", "Singer", "Artis 23", 5, 5, 2));
        mList.add(new item(R.drawable.artist_image_1, "Ed Sheeran2", "Singer", "Artis 23", 15, 15, 12));
        mList.add(new item(R.drawable.artist_image_2, "Ed Sheeran3", "Singer", "Artis 23", 25, 25, 22));
        mList.add(new item(R.drawable.artist_image_3, "Ed Sheeran4", "Singer", "Artis 23", 35, 35, 32));
        mList.add(new item(R.drawable.artist_image_3, "Ed Dym", "Singer", "Artis 24", 45, 45, 42));
        mList.add(new item(R.drawable.artist_image_3, "Pan Ntym", "Singer", "Artis 24", 55, 55, 52));

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
