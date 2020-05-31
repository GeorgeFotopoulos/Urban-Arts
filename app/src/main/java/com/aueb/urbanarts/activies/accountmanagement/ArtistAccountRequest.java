package com.aueb.urbanarts.activies.accountmanagement;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aueb.urbanarts.R;
import com.aueb.urbanarts.activies.search.Feed;
import com.aueb.urbanarts.activies.HomePage;
import com.aueb.urbanarts.activies.report.ReportUser;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ArtistAccountRequest extends AppCompatActivity {
    private final List<String> artistType = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    private StorageReference storageReference;
    private final int PICK_IMAGE_REQUEST = 22;
    private final int PERMISSION_CODE = 2342;
    private String photoPath, TAG;
    String indiv_or_group;
    private Uri filePath;
    Spinner sItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_artist_profile);
        requestStoragePermission();
        storageReference = FirebaseStorage.getInstance().getReference();

        final Button upload = findViewById(R.id.upload_image);
        final EditText description = findViewById(R.id.description);
        final DatePicker calendar = findViewById(R.id.calendar);
        final TextView years = findViewById(R.id.years);
        final TextView group = findViewById(R.id.group);
        final TextView individual = findViewById(R.id.individual);
        individual.setTextColor(Color.parseColor("#b71e42"));
        group.setTextColor(Color.parseColor("#808080"));
        indiv_or_group = "individual";

        getArtistTypes();

        findViewById(R.id.upload_image).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (v == upload) {
                    showFileChooser();
                }
            }
        });

        findViewById(R.id.indiv_group).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if ((years.getText().toString()).equals("Date of Birth:")) {
                    years.setText("Est.");
                    group.setTextColor(Color.parseColor("#b71e42"));
                    individual.setTextColor(Color.parseColor("#808080"));
                    indiv_or_group = "group";
                } else {
                    years.setText("Date of Birth:");
                    individual.setTextColor(Color.parseColor("#b71e42"));
                    group.setTextColor(Color.parseColor("#808080"));
                    indiv_or_group = "individual";
                }
            }
        });

        findViewById(R.id.request).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (sItems != null) {
                    String artistType = sItems.getSelectedItem().toString();
                    makeMeArtist(artistType, indiv_or_group, String.valueOf(calendar.getYear()), description.getText().toString());
                }
            }
        });

    }

    private void getArtistTypes() {
        db.collection("genre")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                artistType.add(document.getId());
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(ArtistAccountRequest.this, android.R.layout.simple_spinner_item, artistType);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            sItems = findViewById(R.id.genre);
                            sItems.setAdapter(adapter);
                        } else {
                            Log.d("123", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void addArtist(final String artistType, final String groupType, final String year, final String description, final String imageURL) {
        final Map<String, Object> userMap = new HashMap<>();
        final Map<String, Object> artistMap = new HashMap<>();
        artistMap.put("user_id", user.getUid());

        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    List<String> galleryList = new ArrayList<String>() {
                    };

                    artistMap.put("display_name", document.getString("username"));
                    artistMap.put("genre", artistType);
                    artistMap.put("year", year);
                    artistMap.put("followers", document.getString("followers"));
                    artistMap.put("artist_type", groupType);
                    artistMap.put("profile_image_url", imageURL);
                    artistMap.put("gallery", galleryList);

                    if (description.equals("")) {
                        artistMap.put("description", "No Description.");
                    } else {
                        artistMap.put("description", description);
                    }

                    // Add a new document with the user's ID
                    final ConstraintLayout dialog = findViewById(R.id.dialog);
                    final ConstraintLayout wholeLayout = findViewById(R.id.constraint);
                    db.collection("artists")
                            .document(user.getUid())
                            .set(artistMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "Artist Account Made!", Toast.LENGTH_LONG).show();
                                    userMap.put("is_artist", true);
                                    db.collection("users").document(user.getUid()).update(userMap);
                                    goHomePage();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("123", "Error adding document", e);
                                    Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    Log.d("123", "get failed with ", task.getException());
                }
            }
        });
    }

    public void uploadImage(final String artistType, final String indiv_or_group, final String year, final String description) {
        final ConstraintLayout dialog = findViewById(R.id.dialog);
        final ConstraintLayout wholeLayout = findViewById(R.id.constraint);
        dialog.setVisibility(View.VISIBLE);
        wholeLayout.setBackgroundColor(Color.parseColor("#808080"));

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        final StorageReference profilesRef = storageReference.child("profiles/profile" + user.getUid() + ".jpg");

        final TextView percentage = findViewById(R.id.perc);
        profilesRef.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplicationContext(), "File Uploaded", Toast.LENGTH_LONG).show();
                percentage.setText("Getting your Artist Account ready...");
                dialog.setVisibility(View.VISIBLE);
                wholeLayout.setBackgroundColor(Color.parseColor("#808080"));

                profilesRef.putFile(filePath).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        // Continue with the task to get the download URL
                        return profilesRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            if (downloadUri == null)
                                return;
                            else {
                                photoPath = downloadUri.toString();
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                addArtist(artistType, indiv_or_group, year, description, photoPath);
                            }
                        }
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                dialog.setVisibility(View.INVISIBLE);
                wholeLayout.setBackgroundColor(Color.parseColor("#fffff"));
                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double prog = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                percentage.setText("Uploading... " + ((int) prog) + "%");
            }
        });
    }

    public void makeMeArtist(final String artistType, String indiv_or_group, final String year, final String description) {
        if (checkToMakeArtist(artistType, indiv_or_group, year)) {
            if (filePath != null) {
                uploadImage(artistType, indiv_or_group, year, description);
            } else {
                String unknownArtistURL = "none";
                addArtist(artistType, indiv_or_group, year, description, unknownArtistURL);
            }
        }
    }

    private boolean checkToMakeArtist(String artistType, String indiv_or_group, String year) {
        final TextView percentage = findViewById(R.id.perc);
        int findAge = Calendar.getInstance().get(Calendar.YEAR) - Integer.parseInt(year);
        if (!artistType.equals("")) {
            if (indiv_or_group.equals("individual")) {
                if (findAge >= 16) {
                    percentage.setText("Waiting to finish...");
                    final ConstraintLayout dialog = findViewById(R.id.dialog);
                    final ConstraintLayout wholeLayout = findViewById(R.id.constraint);
                    dialog.setVisibility(View.VISIBLE);
                    wholeLayout.setBackgroundColor(Color.parseColor("#808080"));

                    return true;
                } else {
                    Toast.makeText(getApplicationContext(), "You have to be at least 16 years old!", Toast.LENGTH_LONG).show();
                    return false;
                }
            } else {
                if (findAge >= 0) {
                    percentage.setText("Waiting to finish...");
                    final ConstraintLayout dialog = findViewById(R.id.dialog);
                    final ConstraintLayout wholeLayout = findViewById(R.id.constraint);
                    dialog.setVisibility(View.VISIBLE);
                    wholeLayout.setBackgroundColor(Color.parseColor("#808080"));

                    return true;
                } else {
                    Toast.makeText(getApplicationContext(), "Wrong Year...", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    protected void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted!", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(this, "Permission Not Granted!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                filePath = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                ImageView image = findViewById(R.id.image);
                image.setImageBitmap(bitmap);
            } catch (IOException e) {

            }
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
        Animatoo.animateFade(this);
        finish();
    }

    private void goHomePage() {
        Intent myIntent = new Intent(ArtistAccountRequest.this, HomePage.class);
        startActivity(myIntent);
        Animatoo.animateZoom(this);
        finish();
    }

    public static class ArtistProfileActivity extends AppCompatActivity {
        final String TAG = "123";
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CarouselView carouselView;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String artistName;
        String artistType;
        String artistGenre;
        String artistYear;
        String followersNum;
        String artistDescription;
        String artistImage;
        String artist_id;
        String text;
        List<String> artistGallery = new ArrayList<>();
        Map<String, Boolean> followedUsersMap = new HashMap<>();
        TextView followersNumDisplay;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_artist_profile);

            ProgressBar imageProg = findViewById(R.id.progress_bar);
            imageProg.setVisibility(View.VISIBLE);

            Intent intent = getIntent();
            artist_id = intent.getStringExtra("ARTIST_DOCUMENT_ID");
            whoIsIt(artist_id);
            getArtistInformation(artist_id);

            carouselView = findViewById(R.id.gallery);
            carouselView.setPageCount(artistGallery.size());
            carouselView.setImageListener(imageListener);

            findViewById(R.id.action).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (artist_id.equals(user.getUid())) {
                        goEditAccount();
                    } else {
                        goReportUser(artist_id);
                    }
                }
            });

            findViewById(R.id.home_button).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    goHomePage();
                }
            });

            findViewById(R.id.go_feed).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    goFeed(artist_id, artistName, artistImage);
                }
            });

            TextView appName = findViewById(R.id.appName);
            appName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ArtistProfileActivity.this, HomePage.class);
                    startActivity(intent);
                    Animatoo.animateZoom(ArtistProfileActivity.this);
                    finish();
                }
            });

            final Button follow = findViewById(R.id.follow_button);

            if (user != null) {
                DocumentReference docRef = db.collection("users").document(mAuth.getCurrentUser().getUid());
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            final DocumentSnapshot document = task.getResult();
                            followedUsersMap = (Map<String, Boolean>) document.get("followedUsers");
                            try {
                                if (followedUsersMap.get(artist_id)) {
                                    text = "Unfollow";
                                    follow.setText(text);
                                } else {
                                    text = "Follow";
                                    follow.setText(text);
                                }
                            } catch (Exception ignore) {
                            }
                        }
                    }
                });
            } else {
                follow.setClickable(false);
            }

            follow.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (user == null) {
                        Toast.makeText(ArtistProfileActivity.this, "You have to create an account in order to follow an artist!", Toast.LENGTH_SHORT).show();
                    } else {
                        final DocumentReference docRef = db.collection("users").document(mAuth.getCurrentUser().getUid());
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    final DocumentSnapshot document = task.getResult();
                                    followedUsersMap = (Map<String, Boolean>) document.get("followedUsers");
                                    final DocumentReference docRef2 = db.collection("artists").document(artist_id);
                                    docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                final DocumentSnapshot documentSnapshot = task.getResult();
                                                if (followedUsersMap.containsKey(artist_id)) {
                                                    if (followedUsersMap.get(artist_id)) {
                                                        showFollowers(followersNumDisplay, String.valueOf(Integer.parseInt(documentSnapshot.getString("followers")) - 1));
                                                        docRef2.update("followers", String.valueOf(Integer.parseInt(documentSnapshot.getString("followers")) - 1));
                                                        followedUsersMap.put(artist_id, false);
                                                        docRef.update("followedUsers", followedUsersMap);
                                                        text = "Follow";
                                                        follow.setText(text);
                                                    } else {
                                                        showFollowers(followersNumDisplay, String.valueOf(Integer.parseInt(documentSnapshot.getString("followers")) + 1));
                                                        docRef2.update("followers", String.valueOf(Integer.parseInt(documentSnapshot.getString("followers")) + 1));
                                                        followedUsersMap.put(artist_id, true);
                                                        docRef.update("followedUsers", followedUsersMap);
                                                        text = "Unfollow";
                                                        follow.setText(text);
                                                    }
                                                } else {
                                                    followedUsersMap.put(artist_id, true);
                                                    showFollowers(followersNumDisplay, String.valueOf(Integer.parseInt(documentSnapshot.getString("followers")) + 1));
                                                    docRef2.update("followers", String.valueOf(Integer.parseInt(documentSnapshot.getString("followers")) + 1));
                                                    followedUsersMap.put(artist_id, true);
                                                    docRef.update("followedUsers", followedUsersMap);
                                                    text = "Unfollow";
                                                    follow.setText(text);
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        }

        private void whoIsIt(String artist_id) {
            if (user != null) {
                ImageButton action = findViewById(R.id.action);
                if (artist_id.equals(user.getUid())) {
                    action.setImageResource(R.drawable.edit);
                } else {
                    action.setImageResource(R.drawable.report);
                }
            }
        }

        private void getArtistInformation(final String artist_id) {
            final CircleImageView profileImage = findViewById(R.id.artist_profile_photo);
            final TextView artistNameDisplay = findViewById(R.id.artist_name);
            final TextView genreDisplay = findViewById(R.id.artist_genre);
            final TextView yearDisplay = findViewById(R.id.artist_age);
            final TextView descriptionDisplay = findViewById(R.id.artist_description);
            followersNumDisplay = findViewById(R.id.num_follows);
            final ProgressBar loadGallery = findViewById(R.id.load_carousel);
            final CarouselView gallery = findViewById(R.id.gallery);

            DocumentReference docArtist = db.collection("artists").document(artist_id);
            docArtist.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            getArtistEvents(artist_id);
                            artistName = document.getString("display_name");
                            artistType = document.getString("artist_type");
                            artistGenre = document.getString("genre");
                            artistYear = document.getString("year");
                            artistDescription = document.getString("description");
                            artistName = document.getString("display_name");
                            artistImage = document.getString("profile_image_url");
                            followersNum = document.getString("followers");
                            if (!document.get("gallery").equals(""))
                                artistGallery = (List<String>) document.get("gallery");

                            showProfileImage(profileImage, artistImage);
                            showArtistName(artistNameDisplay, artistName);
                            showGenre(genreDisplay, artistGenre);
                            showYear(yearDisplay, artistYear);
                            showDescription(descriptionDisplay, artistDescription);
                            showFollowers(followersNumDisplay, followersNum);

                            TextView noGallery = findViewById(R.id.error_gallery);
                            if (!artistGallery.isEmpty()) {
                                noGallery.setText("");
                                showGallery(artistGallery);

                                final float scale = getResources().getDisplayMetrics().density;
                                int pixels = (int) (300 * scale);
                                gallery.requestLayout();
                                gallery.getLayoutParams().height = pixels;

                            } else {
                                noGallery.setText("No Images :(");
                                loadGallery.setVisibility(View.INVISIBLE);
                                loadGallery.setVisibility(View.GONE);
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

        private void getArtistEvents(final String artist_id) {
            final ImageView goFeedDisplay = findViewById(R.id.go_feed);
            final TextView eventsNumDisplay = findViewById(R.id.events_num);
            final int[] counter = {0};
            db.collection("events")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (document.getString("ArtistID").equals(artist_id)) {
                                        counter[0]++;
                                    }
                                }

                                if (counter[0] == 0) {
                                    eventsNumDisplay.setText("No events.");
                                } else if (counter[0] == 1) {
                                    goFeedDisplay.setVisibility(View.VISIBLE);
                                    eventsNumDisplay.setText("1 event.");
                                } else {
                                    goFeedDisplay.setVisibility(View.VISIBLE);
                                    eventsNumDisplay.setText(counter[0] + " events.");
                                }
                            } else {
                                Log.w(TAG, "Error getting documents.", task.getException());
                            }
                        }
                    });
        }

        private void showFollowers(final TextView followersNumDisplay, final String followers) {
            followersNumDisplay.setText(followers);
        }

        private void showDescription(TextView descriptionDisplay, final String description) {
            descriptionDisplay.setText(description);
        }

        private void showYear(final TextView yearDisplay, final String year) {
            String whatYear = String.valueOf(year);
            String type = String.valueOf(artistType);
            String whatToSay = "";

            if (type.equals("individual")) {
                int findAge = Calendar.getInstance().get(Calendar.YEAR) - Integer.parseInt(whatYear);
                whatToSay = findAge + " years old.";
            } else {
                int findAge = Calendar.getInstance().get(Calendar.YEAR) - Integer.parseInt(whatYear);
                if (findAge == 0) {
                    whatToSay = "almost 1 year together.";
                } else if (findAge > 1) {
                    whatToSay = findAge + " years together.";
                } else if (findAge == 1) {
                    whatToSay = "1 year together.";
                }
            }
            yearDisplay.setText(whatToSay);
        }

        private void showGenre(TextView genreDisplay, final String genre) {
            genreDisplay.setText(genre);
        }

        private void showArtistName(TextView nameDisplay, final String name) {
            nameDisplay.setText(name);
        }

        private void showProfileImage(final CircleImageView profileImage, final String imageURL) {
            final ProgressBar imageProg = findViewById(R.id.progress_bar);
            if (imageURL.equals("none")) {
                profileImage.setImageResource(R.drawable.profile);
                imageProg.setVisibility(View.INVISIBLE);
            } else {
                Glide.with(getApplicationContext())
                        .load(imageURL)
                        .listener(new RequestListener() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                                imageProg.setVisibility(View.INVISIBLE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                                imageProg.setVisibility(View.INVISIBLE);
                                return false;
                            }
                        })
                        .into(profileImage);
            }
        }

        private void showGallery(List<String> artistGallery) {
            carouselView.setPageCount(artistGallery.size());
            carouselView.setImageListener(imageListener);
        }

        ImageListener imageListener = new ImageListener() {
            @Override
            public void setImageForPosition(int position, ImageView imageView) {
                final ProgressBar loadGallery = findViewById(R.id.load_carousel);
                Glide.with(getApplicationContext())
                        .load(artistGallery.get(position))
                        .listener(new RequestListener() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                                loadGallery.setVisibility(View.INVISIBLE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                                loadGallery.setVisibility(View.INVISIBLE);
                                return false;
                            }
                        })
                        .into(imageView);
            }
        };

        @Override
        public void onBackPressed() {
            super.onBackPressed();
            Animatoo.animateFade(this);
            finish();
        }

        public void goHomePage() {
            startActivity(new Intent(this, HomePage.class));
            Animatoo.animateZoom(this);
            finish();
        }

        private void goEditAccount() {
            startActivity(new Intent(this, EditAccount.class));
            Animatoo.animateFade(this);
            finish();
        }

        private void goFeed(String artist_id, String artist_name, String artist_image) {
            Intent intent = new Intent(ArtistProfileActivity.this, Feed.class);
            intent.putExtra("artist_id", artist_id);
            intent.putExtra("artist_name", artist_name);
            intent.putExtra("artist_image", artist_image);
            intent.putExtra("FROM_ARTIST", "FROM_ARTIST");
            startActivity(intent);
            Animatoo.animateFade(this);
        }

        private void goReportUser(String artist_id) {
            Intent intent = new Intent(ArtistProfileActivity.this, ReportUser.class);
            intent.putExtra("artist_id", artist_id);
            startActivity(intent);
            Animatoo.animateFade(this);
        }
    }
}