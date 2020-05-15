package com.aueb.urbanarts;

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
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    private final String uknownArtistURL = "https://firebasestorage.googleapis.com/v0/b/aeps-a0e6f.appspot.com/o/profiles%2Fuknown_artist.png?alt=media&token=ea50f1a2-5c68-4b63-b8a4-6125d9727905";
    private boolean newImageChosen = false;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    Spinner sItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_artist_profile);
        requestStoragePermission();
        storageReference = FirebaseStorage.getInstance().getReference();

        final ProgressBar progress = findViewById(R.id.progressBar);
        final TextView percentage = findViewById(R.id.perc);
        percentage.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.INVISIBLE);

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

        final List<String> artist_type = new ArrayList<>();

        fStore.collection("artist_type")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                artist_type.add(document.getId() + "");

                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(ArtistAccountRequestActivity.this, android.R.layout.simple_spinner_item, artist_type);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            sItems = (Spinner) findViewById(R.id.genre);
                            sItems.setAdapter(adapter);
                        } else {
                            Log.d("123", "Error getting documents: ", task.getException());
                        }
                    }
                });


        findViewById(R.id.upload_image).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (v == upload) {
                    showFileChooser();
                }
            }
        });


        indiv_group.setOnClickListener(new View.OnClickListener() {
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
                    uploadPhoto(artistType, indiv_or_group, String.valueOf(calendar.getYear()), description.getText().toString());
                }


            }
        });

    }

    public void makeArtist(final String artistType, final String groupType, final String year, final String description) {
        final Map<String, Object> userMap = new HashMap<>();

        final Map<String, Object> artist = new HashMap<>();
        artist.put("user_id", user.getUid());

        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    artist.put("display_name", document.getString("username"));

                    artist.put("genre", artistType);
                    artist.put("year", year);
                    artist.put("followers", 0);
                    artist.put("artist_type", groupType);
                    if (!newImageChosen) {
                        artist.put("profile_image_url", uknownArtistURL);
                    } else {
                        artist.put("profile_image_url", photoPath);
                    }
                    artist.put("gallery", "");


                    userMap.put("is_artist", true);
                    if (description.equals("")) {
                        artist.put("description", "No Description.");
                    } else {
                        artist.put("description", description);
                    }

                    // Add a new document with a generated ID
                    final ProgressBar progress = findViewById(R.id.progressBar);
                    final TextView percentage = findViewById(R.id.perc);
                    db.collection("artists")
                            .add(artist)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d("123", "DocumentSnapshot added with ID: " + documentReference.getId());
                                    Toast.makeText(getApplicationContext(), "Artist Account Made!", Toast.LENGTH_LONG).show();
                                    percentage.setVisibility(View.INVISIBLE);
                                    progress.setVisibility(View.INVISIBLE);
                                    finish();
                                    Intent myIntent = new Intent(ArtistAccountRequestActivity.this, HomePage.class);
                                    startActivity(myIntent);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("123", "Error adding document", e);
                                    Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
                                }
                            });

                    // Update user account to artist
                    db.collection("users").document(user.getUid()).update(userMap);


                } else {
                    Log.d("123", "get failed with ", task.getException());
                }
            }
        });


    }

    public void uploadPhoto(final String artistType, String groupType, final String year, final String description) {

        if (filePath != null) {
            final ProgressBar progress = findViewById(R.id.progressBar);
            final TextView percentage = findViewById(R.id.perc);
            percentage.setVisibility(View.VISIBLE);
            progress.setVisibility(View.VISIBLE);
            percentage.setText("Uploading...");
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            final StorageReference riversRef = storageReference.child("profiles/profile" + user.getUid() + ".jpg");


            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            percentage.setVisibility(View.INVISIBLE);
                            progress.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "File Uploaded", Toast.LENGTH_LONG).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            percentage.setVisibility(View.INVISIBLE);
                            progress.setVisibility(View.INVISIBLE);
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


            riversRef.putFile(filePath).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {

                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return riversRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        if (downloadUri == null)
                            return;
                        else {
                            if (!artistType.equals("")) {
                                percentage.setText("Waiting to finish...");
                                percentage.setVisibility(View.VISIBLE);
                                progress.setVisibility(View.VISIBLE);
                                photoPath = String.valueOf(downloadUri);
                                makeArtist(artistType, indiv_or_group, year, description);
                            }
                        }

                    }
                }
            });
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        } else {
            makeArtist(artistType, indiv_or_group, year, description);
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
            filePath = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                ImageView image = findViewById(R.id.image);
                image.setImageBitmap(bitmap);
                newImageChosen = true;
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


}
