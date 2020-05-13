package com.aueb.urbanarts;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

        ImageView signIn = findViewById(R.id.logIn);
        if (mAuth.getCurrentUser() != null) {
            signIn.setImageResource(R.drawable.log_out);
            tempStr = "Log Out";
            temp = findViewById(R.id.tv_logIn);
            temp.setText(tempStr);
            signIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuth.signOut();
                    // kane log out
                    Intent myIntent = new Intent(HomePage.this, HomePage.class);
                    HomePage.this.startActivity(myIntent);
                }
            });
        } else {
            signIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(HomePage.this, LogIn.class);
                    HomePage.this.startActivity(myIntent);
                }
            });
        }

        ImageView register = findViewById(R.id.register);
        if (mAuth.getCurrentUser() != null) {
            register.setImageResource(R.drawable.edit_account);
            tempStr = "Edit Account";
            temp = findViewById(R.id.tv_register);
            temp.setText(tempStr);
            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Stelne me sto EditAccountActivity
                    // Intent myIntent = new Intent(HomePage.this, EditAccountActivity.class);
                    // HomePage.this.startActivity(myIntent);
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

        ImageView filters = findViewById(R.id.filters);
        filters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HomePage.this, SearchFilters.class);
                HomePage.this.startActivity(myIntent);
            }
        });

        ImageView live = findViewById(R.id.liveFeed);
        live.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HomePage.this, Feed.class);
                HomePage.this.startActivity(myIntent);
            }
        });

        ImageView createPost = findViewById(R.id.createPost);
        createPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HomePage.this, PostSomething.class);
                HomePage.this.startActivity(myIntent);
            }
        });

        ImageView favorites = findViewById(R.id.favorites);
        favorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HomePage.this, ShowPostOnMapActivity.class);
                HomePage.this.startActivity(myIntent);
            }
        });
    }

}