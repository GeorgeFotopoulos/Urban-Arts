package com.aueb.urbanarts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArtistAccountRequestActivity extends AppCompatActivity {

    private final int PERMISSION_CODE = 2342;
    private final int PICK_IMAGE_REQUEST = 22;
    private Uri filePath;
    private String photoPath;
    private Bitmap bitmap;
    private StorageReference storageReference;
    String indiv_or_group;
    private final List<String> artistType = new ArrayList<>();
    private final String uknownArtistURL = "none";
    private boolean newImageChosen = false;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Spinner sItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_artist_profile);
        requestStoragePermission();
        storageReference = FirebaseStorage.getInstance().getReference();

        final Switch indiv_group = findViewById(R.id.indiv_group);
        final Button upload = findViewById(R.id.upload_image);
        final TextView fileName = findViewById(R.id.file_name);
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

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(ArtistAccountRequestActivity.this, android.R.layout.simple_spinner_item, artistType);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            sItems = (Spinner) findViewById(R.id.genre);
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
                    artistMap.put("followers", "0");
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

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        final StorageReference profilesRef = storageReference.child("profiles/profile" + user.getUid() + ".jpg");

        final TextView percentage = findViewById(R.id.perc);
        profilesRef.putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {

                        dialog.setVisibility(View.INVISIBLE);
                        wholeLayout.setBackgroundColor(Color.parseColor("#fffff"));
                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
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
                addArtist(artistType, indiv_or_group, year, description, uknownArtistURL);
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
//                Toast.makeText(this, "Permission Not Granted!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                filePath = data.getData();
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
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
        Intent intent = new Intent(ArtistAccountRequestActivity.this, HomePage.class);
        startActivity(intent);
        Animatoo.animateZoom(this);
        finish();
    }

    private void goHomePage() {
        Intent myIntent = new Intent(ArtistAccountRequestActivity.this, HomePage.class);
        startActivity(myIntent);
        Animatoo.animateZoom(this);
        finish();
    }

}