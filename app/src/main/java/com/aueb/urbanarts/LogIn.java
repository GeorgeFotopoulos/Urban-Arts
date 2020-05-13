package com.aueb.urbanarts;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogIn extends AppCompatActivity {
    //private FirebaseAuth mAuth;

    //public LogIn(FirebaseAuth mAuth) {
    //    this.mAuth = mAuth;
    //}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        //mAuth = FirebaseAuth.getInstance();
    }

    //@Override
    //public void onStart() {
    //    super.onStart();
    //    // Check if user is signed in (non-null) and update UI accordingly.
    //    FirebaseUser currentUser = mAuth.getCurrentUser();
    //    //updateUI(currentUser);
    //}
}
