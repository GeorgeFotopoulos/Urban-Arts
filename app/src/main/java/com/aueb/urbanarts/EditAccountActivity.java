package com.aueb.urbanarts;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;

public class EditAccountActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("123", document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w("123", "Error getting documents.", task.getException());
                        }
                    }
                });

        try {

            final DocumentSnapshot[] userDoc = new DocumentSnapshot[1];
            db.collection("users")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("123", document.getId() + " => " + document.getData());
                                    if(document.getId().equals(user.getUid())){
                                        userDoc[0] = document;
                                    }
                                }

                                if (userDoc[0].getBoolean("is_artist")) {
                                    setContentView(R.layout.edit_artist_account);
                                } else {
                                    setContentView(R.layout.edit_user_account);
                                }

                            } else {
                                Log.w("123", "Error getting documents.", task.getException());
                            }
                        }
                    });

        } catch (Exception e) {
            setContentView(R.layout.edit_user_account);
        }


    }
}
