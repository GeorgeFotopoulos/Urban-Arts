package com.aueb.urbanarts;

import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostSomething extends AppCompatActivity {
    String TAG, name = "", typeOfArt = "", live = "", comment = "";
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private List<ExampleItem> AppArtists = new ArrayList<>();
    private static final int PICK_IMAGE_REQUEST = 22;
    public static String ID = "";
    Button btnUpload, btnProceed;
    EditText tv_name, tv_comment;
    private FirebaseAuth mAuth;
    boolean yesFilter = false;
    private Uri filePath;
    Switch aSwitch;
    Spinner sItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.make_post);
        retrieveList();
        mAuth = FirebaseAuth.getInstance();
        final List<String> genres = new ArrayList<>();

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
                                        Intent myIntent = new Intent(PostSomething.this, ArtistProfileActivity.class);
                                        myIntent.putExtra("ARTIST_DOCUMENT_ID", mAuth.getUid());
                                        startActivity(myIntent);
                                        Animatoo.animateFade(PostSomething.this);
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
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(PostSomething.this, android.R.layout.simple_spinner_item, genres);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            sItems = findViewById(R.id.genreSpinner);
                            sItems.setAdapter(adapter);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        btnUpload = findViewById(R.id.uploadFile);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (v == btnUpload) {
                    showFileChooser();
                    yesFilter = true;
                }
            }
        });

        btnProceed = findViewById(R.id.proceed);
        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_name = findViewById(R.id.actv);
                if (!TextUtils.isEmpty(tv_name.getText())) {
                    name = tv_name.getText().toString().trim();
                } else {
                    name = "";
                }

                tv_comment = findViewById(R.id.comment);
                if (!TextUtils.isEmpty(tv_comment.getText())) {
                    comment = tv_comment.getText().toString().trim();
                } else {
                    comment = "";
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
                } else if (!aSwitch.isChecked()) {
                    live = "false";
                }

                if (!typeOfArt.contains("Choose")) {
                    DocumentReference docUser = fStore.collection("users").document(mAuth.getUid());
                    docUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {

                                    String userName = document.getString("username");
                                    Intent intent = new Intent(PostSomething.this, ShowPostOnMapActivity.class);
                                    if (!name.contains(" (UA User)")) {
                                        ID = "";
                                    } else {
                                        name = name.replace(" (UA User)", "");
                                    }
                                    intent.putExtra("name", name);
                                    intent.putExtra("postedBy", userName);
                                    intent.putExtra("comment", comment);
                                    intent.putExtra("typeOfArt", typeOfArt);
                                    intent.putExtra("live", live);
                                    intent.putExtra("ID", ID);
                                    if (filePath != null) {
                                        intent.putExtra("filePath", filePath.toString());
                                    }
                                    startActivity(intent);
                                    Animatoo.animateFade(PostSomething.this);
                                }
                            }
                        }
                    });
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Field \"Art Genre\" is required.", Toast.LENGTH_LONG);
                    toast.show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            TextView filepath = findViewById(R.id.filepath);
            filepath.setText("Image File: " + filePath);
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(PostSomething.this, HomePage.class);
        startActivity(intent);
        Animatoo.animateZoom(PostSomething.this);
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