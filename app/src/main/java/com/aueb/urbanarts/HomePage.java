package com.aueb.urbanarts;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;

public class HomePage extends AppCompatActivity {
    private FirebaseAuth mAuth;
    String tempStr = "";
    TextView temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        mAuth = FirebaseAuth.getInstance();

        ImageView loginImg = findViewById(R.id.logIn);
        CardView login = findViewById(R.id.cardView1);
        if (mAuth.getCurrentUser() != null) {
            loginImg.setImageResource(R.drawable.log_out);
            tempStr = "Log Out";
            temp = findViewById(R.id.tv_logIn);
            temp.setText(tempStr);
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuth.signOut();
                    Intent myIntent = new Intent(HomePage.this, HomePage.class);
                    HomePage.this.startActivity(myIntent);
                }
            });
        } else {
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(HomePage.this, LogIn.class);
                    HomePage.this.startActivity(myIntent);
                }
            });
        }

        ImageView registerImg = findViewById(R.id.register);
        CardView register = findViewById(R.id.cardView2);
        if (mAuth.getCurrentUser() != null) {
            registerImg.setImageResource(R.drawable.edit_account);
            tempStr = "My Account";
            temp = findViewById(R.id.tv_register);
            temp.setText(tempStr);
            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                     Intent myIntent = new Intent(HomePage.this, EditAccountActivity.class);
                     startActivity(myIntent);
                }
            });
        } else {
            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(HomePage.this, SignUp.class);
                    HomePage.this.startActivity(myIntent);
                }
            });
        }

        CardView filters = findViewById(R.id.cardView3);
        filters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HomePage.this, SearchFilters.class);
                HomePage.this.startActivity(myIntent);
            }
        });

        CardView live = findViewById(R.id.cardView4);
        live.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HomePage.this, Feed.class);
                HomePage.this.startActivity(myIntent);
            }
        });

        CardView createPost = findViewById(R.id.cardView5);
        createPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HomePage.this, PostSomething.class);
                HomePage.this.startActivity(myIntent);
            }
        });

        CardView favorites = findViewById(R.id.cardView6);
        favorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HomePage.this, ArtistAccountRequestActivity.class);
                HomePage.this.startActivity(myIntent);
            }
        });
    }

}