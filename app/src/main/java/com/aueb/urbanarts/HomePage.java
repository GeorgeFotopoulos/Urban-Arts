package com.aueb.urbanarts;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        ImageView register = (ImageView) findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HomePage.this, SignUp.class);
                HomePage.this.startActivity(myIntent);
            }
        });

         ImageView signIn = (ImageView) findViewById(R.id.logIn);
         signIn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent myIntent = new Intent(HomePage.this, LogIn.class);
                 HomePage.this.startActivity(myIntent);
             }
         });

        ImageView filters = (ImageView) findViewById(R.id.filters);
        filters.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HomePage.this, ArtistProfileActivity.class);
                HomePage.this.startActivity(myIntent);
            }
        });

        ImageView live = (ImageView) findViewById(R.id.liveFeed);
        live.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HomePage.this, Feed.class);
                HomePage.this.startActivity(myIntent);
            }
        });

        ImageView createPost = (ImageView) findViewById(R.id.createPost);
        createPost.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HomePage.this, ShowMapActivity.class);
                HomePage.this.startActivity(myIntent);
            }
        });

        ImageView favorites = (ImageView) findViewById(R.id.favorites);
        favorites.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HomePage.this, ShowPostOnMapActivity.class);
                HomePage.this.startActivity(myIntent);
            }
        });
    }


}
