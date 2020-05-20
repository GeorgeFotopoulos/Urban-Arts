package com.aueb.urbanarts;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchFilters extends AppCompatActivity {
    String TAG, location = "", name = "", typeOfArt = "", live = "";
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    EditText tv_location, tv_name;
    boolean yesFilter = false;
    FirebaseAuth mAuth;
    Button btnSearch;
    Switch aSwitch;
    Spinner sItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_with_filters);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            final ProgressBar loadingImage = findViewById(R.id.loading_image);
            loadingImage.setVisibility(View.VISIBLE);
            showUserInfo(loadingImage);
        } else {
            final ProgressBar loadingImage = findViewById(R.id.loading_image);
            loadingImage.setVisibility(View.INVISIBLE);
        }

        findViewById(R.id.account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null) {
                    DocumentReference docUser = database.collection("users").document(mAuth.getUid());
                    docUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    if (document.getBoolean("is_artist")) {
                                        Intent myIntent = new Intent(SearchFilters.this, ArtistProfileActivity.class);
                                        myIntent.putExtra("ARTIST_DOCUMENT_ID", mAuth.getUid());
                                        startActivity(myIntent);
                                        Animatoo.animateFade(SearchFilters.this);
                                        finish();
                                    }
                                } else {
                                    Log.d(TAG, "get failed with ", task.getException());
                                }
                            }
                        }
                    });
                }
            }
        });

        final List<String> genres = new ArrayList<>();
        fStore.collection("genre")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            genres.add("Choose...");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                genres.add(document.getId() + "");
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(SearchFilters.this, android.R.layout.simple_spinner_item, genres);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            sItems = findViewById(R.id.genreSpinner);
                            sItems.setAdapter(adapter);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        btnSearch = findViewById(R.id.search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_location = findViewById(R.id.location);
                if (!TextUtils.isEmpty(tv_location.getText())) {
                    location = tv_location.getText().toString().trim();
                    yesFilter = true;
                }

                tv_name = findViewById(R.id.name);
                if (!TextUtils.isEmpty(tv_name.getText())) {
                    name = tv_name.getText().toString().trim();
                    yesFilter = true;
                }

                if (sItems != null) {
                    typeOfArt = sItems.getSelectedItem().toString().trim();
                    if (!typeOfArt.equals("Choose...")) {
                        yesFilter = true;
                    }
                }

                aSwitch = findViewById(R.id.aSwitch);
                if (aSwitch.isChecked()) {
                    live = "true";
                    yesFilter = true;
                } else if (!aSwitch.isChecked()) {
                    live = "false";
                    yesFilter = true;
                }

                if (yesFilter) {
                    Intent intent = new Intent(SearchFilters.this, Feed.class);
                    if (!location.equals("")) intent.putExtra("location", location);
                    if (!name.equals("")) intent.putExtra("name", name);
                    if (!typeOfArt.equals("Choose...")) intent.putExtra("typeOfArt", typeOfArt);
                    if (!live.equals("")) {
                        intent.putExtra("live", live);
                    }
                    startActivity(intent);
                    Animatoo.animateFade(SearchFilters.this);
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "No filters were selected.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SearchFilters.this, HomePage.class);
        startActivity(intent);
        Animatoo.animateZoom(this);
        finish();
    }

    private void showUserInfo(final ProgressBar loadingImage) {
        DocumentReference docUser = database.collection("users").document(mAuth.getUid());
        docUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String userName = document.getString("username");
                        TextView displayName = findViewById(R.id.username_display);
                        final CircleImageView accountImage = findViewById(R.id.account);
                        displayName.setSelected(true);
                        displayName.setText(userName);
                        if (document.getBoolean("is_artist")) {
                            loadingImage.setVisibility(View.VISIBLE);
                            DocumentReference docArtist = database.collection("artists").document(mAuth.getUid());
                            docArtist.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            if (!document.getString("profile_image_url").equals("none")) {
                                                Glide.with(getApplicationContext())
                                                        .load(document.getString("profile_image_url"))
                                                        .listener(new RequestListener() {
                                                            @Override
                                                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                                                                loadingImage.setVisibility(View.INVISIBLE);
                                                                return false;
                                                            }

                                                            @Override
                                                            public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                                                                loadingImage.setVisibility(View.INVISIBLE);
                                                                return false;
                                                            }
                                                        })
                                                        .into(accountImage);
                                            } else {
                                                accountImage.setImageResource(R.drawable.profile);
                                                loadingImage.setVisibility(View.INVISIBLE);
                                            }
                                        } else {
                                            Log.d(TAG, "No such document");
                                        }
                                    } else {
                                        Log.d(TAG, "get failed with ", task.getException());
                                    }
                                }
                            });
                        } else {
                            accountImage.setImageResource(R.drawable.profile);
                            loadingImage.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
}
