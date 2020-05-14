package com.aueb.urbanarts;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;


import de.hdodenhof.circleimageview.CircleImageView;

public class EditAccountActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                                    if (document.getId().equals(user.getUid())) {
                                        userDoc[0] = document;
                                    }
                                }
                                //User is Artist
                                if (userDoc[0].getBoolean("is_artist")) {
                                    setContentView(R.layout.edit_artist_account);
                                    final ProgressBar imageProg = findViewById(R.id.image_progress);
                                    final CircleImageView profileImage = findViewById(R.id.artist_image);
                                    imageProg.setVisibility(View.VISIBLE);

                                    DocumentReference docRef = db.collection("artists").document(user.getUid());
                                    final String[] imageURL = new String[1];
                                    db.collection("artists")
                                            .whereEqualTo("user_id", user.getUid())
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                            imageURL[0] = String.valueOf(document.get("profile_image_url"));

                                                            Picasso.with(getApplicationContext()).load(imageURL[0]).into(profileImage, new com.squareup.picasso.Callback() {
                                                                @Override
                                                                public void onSuccess() {
                                                                    imageProg.setVisibility(View.INVISIBLE);
                                                                }

                                                                @Override
                                                                public void onError() {

                                                                }
                                                            });

                                                        }

                                                    }
                                                }
                                            });
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
