package com.aueb.urbanarts.activies.search;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aueb.urbanarts.R;
import com.aueb.urbanarts.activies.HomePage;
import com.aueb.urbanarts.activies.accountmanagement.ArtistAccountRequest;
import com.aueb.urbanarts.adapters.SearchAdapter;
import com.aueb.urbanarts.items.ExampleItem;
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

public class SearchArtists extends AppCompatActivity {
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private List<ExampleItem> AppArtists = new ArrayList<>();
    String TAG, name = "", typeOfArt = "";
    public static String ID = "";
    AutoCompleteTextView tv_name;
    private FirebaseAuth mAuth;
    EditText artname;
    Button btnSearch;
    Spinner sItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_artists_with_filters);

        retrieveList();

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
                                        Intent intent = new Intent(SearchArtists.this, ArtistAccountRequest.ArtistProfileActivity.class);
                                        intent.putExtra("ARTIST_DOCUMENT_ID", mAuth.getUid());
                                        startActivity(intent);
                                        Animatoo.animateFade(SearchArtists.this);
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
                Intent intent = new Intent(SearchArtists.this, HomePage.class);
                startActivity(intent);
                Animatoo.animateZoom(SearchArtists.this);
                finish();
            }
        });

        final List<String> genres = new ArrayList<>();
        fStore.collection("genre").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    genres.add("Choose...");
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        genres.add(document.getId() + "");
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(SearchArtists.this, android.R.layout.simple_spinner_item, genres);
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
                artname = findViewById(R.id.artname);
                if (!TextUtils.isEmpty(artname.getText())) {
                    name = artname.getText().toString().trim();
                } else {
                    name = "";
                }
                if (sItems != null) {
                    typeOfArt = sItems.getSelectedItem().toString().trim();
                    if (typeOfArt.equals("Choose...")) {
                        typeOfArt = "";
                    }
                }
                Intent intent = new Intent(SearchArtists.this, SearchArtistsResult.class);
                intent.putExtra("name", name);
                intent.putExtra("typeOfArt", typeOfArt);
                startActivity(intent);
                Animatoo.animateFade(SearchArtists.this);
            }
        });

        findViewById(R.id.searchSpecificArtist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_name = findViewById(R.id.actv);
                if (!TextUtils.isEmpty(tv_name.getText())) {
                    name = tv_name.getText().toString().trim();
                } else {
                    name = "";
                }
                if (!name.contains(" (UA User)")) {
                    ID = "";
                    Toast toast = Toast.makeText(getApplicationContext(), "You should select an Artist from the List", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    try {
                        DocumentReference docRef2 = fStore.collection("artists").document(ID);
                        docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    final DocumentSnapshot documentArt = task.getResult();
                                    if (documentArt.exists()) {
                                        Intent intent = new Intent(SearchArtists.this, ArtistAccountRequest.ArtistProfileActivity.class);
                                        intent.putExtra("ARTIST_DOCUMENT_ID", ID);
                                        Log.d("", ID);
                                        startActivity(intent);
                                        Animatoo.animateFade(SearchArtists.this);
                                    } else {
                                        Toast toast = Toast.makeText(getApplicationContext(), "Oops. Seems that the Artist is no longer using our App", Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Oops. Seems that the Artist is no longer using our App", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }
        });
    }

    private void onsuccess() {
        AutoCompleteTextView editText = findViewById(R.id.actv);
        SearchAdapter adapter = new SearchAdapter(this, AppArtists);
        editText.setAdapter(adapter);
    }

    private void retrieveList() {
        final CollectionReference eventsCollection = fStore.collection("artists");
        eventsCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (final QueryDocumentSnapshot document : task.getResult()) {
                        final ExampleItem itemtoadd = new ExampleItem();
                        itemtoadd.setID(document.getId());
                        DocumentReference docRef = fStore.collection("artists").document(document.getId());
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot artistinfo = task.getResult();
                                    if (artistinfo.exists()) {
                                        itemtoadd.setText1(artistinfo.getString("display_name"));
                                        itemtoadd.setImageResource(artistinfo.getString("profile_image_url"));
                                        itemtoadd.setText2(artistinfo.getString("genre"));
                                    }
                                    AppArtists.add(itemtoadd);
                                    onsuccess();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void showUserInfo(final ProgressBar loadingImage) {
        DocumentReference docUser = fStore.collection("users").document(mAuth.getUid());
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
                            DocumentReference docArtist = fStore.collection("artists").document(mAuth.getUid());
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateFade(this);
        finish();
    }
}