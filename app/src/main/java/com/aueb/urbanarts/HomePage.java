package com.aueb.urbanarts;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

//        ImageView filter = (ImageView) findViewById(R.id.filters);
//        filter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent myIntent = new Intent(HomePage.this, ReportUser.class);
//                HomePage.this.startActivity(myIntent);
//            }
//        });

        ImageView live = (ImageView) findViewById(R.id.liveFeed);
        live.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HomePage.this, ShowMapActivity.class);
                HomePage.this.startActivity(myIntent);
            }
        });
    }


}
