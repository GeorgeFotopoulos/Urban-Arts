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

        //ImageButton live = (ImageButton) findViewById(R.id.liveFeed);
        //live.setOnClickListener(new View.OnClickListener(){
        //    @Override
        //    public void onClick(View v) {
        //        Intent myIntent = new Intent(HomePage.this, ShowMapActivity.class);
        //        HomePage.this.startActivity(myIntent);
        //    }
        //});
    }


}
