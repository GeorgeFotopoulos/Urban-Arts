package com.aueb.urbanarts;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchArtistsResult extends AppCompatActivity {
    String docArtist, docDescription, docProfileImage, docGenre, docYear, docArtistType, TAG;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    List<String> addedArtists = new ArrayList<>();
    List<item> mList = new ArrayList<>();
    private FirebaseAuth mAuth;
    FavoritesAdapter adapter;
    ProgressBar progressBar;
    TextView empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites_feed);

        Intent intent = getIntent();
        final String name = intent.getStringExtra("name");
        final String typeOfArt = intent.getStringExtra("typeOfArt");
        empty = findViewById(R.id.empty);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        adapter = new FavoritesAdapter(SearchArtistsResult.this, mList);
        final RecyclerView recyclerView = findViewById(R.id.recyclerView);

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
                                        Intent intent = new Intent(SearchArtistsResult.this, ArtistProfileActivity.class);
                                        intent.putExtra("ARTIST_DOCUMENT_ID", mAuth.getUid());
                                        startActivity(intent);
                                        Animatoo.animateFade(SearchArtistsResult.this);
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

        TextView appName = findViewById(R.id.appName);
        appName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchArtistsResult.this, HomePage.class);
                startActivity(intent);
                Animatoo.animateZoom(SearchArtistsResult.this);
                finish();
            }
        });

        final CollectionReference artistCollection = database.collection("artists");
        artistCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    boolean tobeadded;
                    for (final QueryDocumentSnapshot document : task.getResult()) {
                        tobeadded = true;
                        docArtist = document.getString("display_name");
                        if (!((docArtist.toLowerCase()).contains(name.toLowerCase()) || name.isEmpty()))
                            tobeadded = false;
                        docGenre = document.getString("genre");
                        if (!(docGenre.equals(typeOfArt) || typeOfArt.isEmpty()))
                            tobeadded = false;
                        docDescription = document.getString("description");
                        try {
                            if (document.getString("profile_image_url").equals("")) {
                                docProfileImage = "none";
                            } else {
                                docProfileImage = document.getString("profile_image_url");
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "Error!");
                        }
                        docYear = document.getString("year");
                        docArtistType = document.getString("artist_type");
                        if (tobeadded) {
                            mList.add(new item(docProfileImage, docArtist, docDescription, docGenre, docYear, docArtistType));
                            addedArtists.add(document.getId());
                        }
                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(SearchArtistsResult.this));
                        adapter.setOnItemClickListener(new FavoritesAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                final String artistID = addedArtists.get(position);
                                DocumentReference docRef = database.collection("artists").document(artistID);
                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                Intent intent = new Intent(SearchArtistsResult.this, ArtistProfileActivity.class);
                                                intent.putExtra("ARTIST_DOCUMENT_ID", artistID);
                                                startActivity(intent);
                                                Animatoo.animateFade(SearchArtistsResult.this);
                                            } else {
                                                Log.d(TAG, "No such document");
                                            }
                                        } else {
                                            Log.d(TAG, "get failed with ", task.getException());
                                        }
                                    }
                                });
                            }
                        });
                    }
                    if (addedArtists.size() == 0) {
                        empty.setVisibility(View.VISIBLE);
                    }
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateFade(this);
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