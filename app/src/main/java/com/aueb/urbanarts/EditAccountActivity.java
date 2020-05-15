package com.aueb.urbanarts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
import com.squareup.picasso.Picasso;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditAccountActivity extends AppCompatActivity {

    private final int PICK_IMAGE_REQUEST = 22;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final String uknownArtistURL = "https://firebasestorage.googleapis.com/v0/b/aeps-a0e6f.appspot.com/o/profiles%2Fuknown_artist.png?alt=media&token=ea50f1a2-5c68-4b63-b8a4-6125d9727905";
    private Bitmap bitmap;
    private String photoPath;
    private boolean changePhoto = false;
    Spinner sItems;
    private Uri filePath;
    final QueryDocumentSnapshot[] artistDoc = new QueryDocumentSnapshot[1];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
// ARTIST
                            if (userDoc[0].getBoolean("is_artist")) {
                                setContentView(R.layout.edit_artist_account);
                                final ProgressBar progress = findViewById(R.id.progressBar);
                                final TextView percentage = findViewById(R.id.perc);
                                percentage.setVisibility(View.INVISIBLE);
                                progress.setVisibility(View.INVISIBLE);

                                final ProgressBar imageProg = findViewById(R.id.image_progress);
                                final CircleImageView profileImage = findViewById(R.id.artist_image);
                                imageProg.setVisibility(View.VISIBLE);

                                final List<String> artist_type = new ArrayList<>();

                                changeGenre(artist_type);
                                showProfileImage(profileImage, imageProg);


                                findViewById(R.id.artist_image).setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        showFileChooser();
                                    }
                                });

                                findViewById(R.id.deactivate).setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        deactivateArtistAccount();
                                    }
                                });

                                findViewById(R.id.terminate).setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        terminateAccount(userDoc);
                                    }
                                });

                                findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        if (sItems != null) {

                                            Map<String, Object> userMap = new HashMap<>();
                                            final Map<String, Object> artistMap = new HashMap<>();
                                            EditText username = findViewById(R.id.username);
                                            EditText oldPassword = findViewById(R.id.old_password);
                                            final EditText newPassword = findViewById(R.id.new_password);
                                            EditText description = findViewById(R.id.description);
                                            String artistType = sItems.getSelectedItem().toString();
                                            final boolean[] changedSomething = {false};

                                            if (!username.getText().toString().equals("")) {
                                                userMap.put("username", username.getText().toString());
                                                changedSomething[0] = true;
                                            }
                                            if (!oldPassword.getText().toString().equals("") && !newPassword.getText().toString().equals("")) {

                                                AuthCredential credential = EmailAuthProvider
                                                        .getCredential(user.getEmail(), oldPassword.getText().toString());

                                                user.reauthenticate(credential)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    user.updatePassword(newPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                Log.d("123", "Password updated");
                                                                                changedSomething[0] = true;
                                                                            } else {
                                                                                Log.d("123", "Error password not updated");
                                                                            }
                                                                        }
                                                                    });
                                                                } else {
                                                                    Log.d("123", "Error auth failed");
                                                                    Toast.makeText(getApplicationContext(), "Wrong Password!", Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });
                                            }
                                            if (!artistType.equals("Choose a Genre...")) {
                                                artistMap.put("genre", artistType);
                                                changedSomething[0] = true;
                                            }
                                            if (!description.getText().toString().equals("")) {
                                                artistMap.put("description", description.getText().toString());
                                                changedSomething[0] = true;
                                            }

                                            if (changedSomething[0] || changePhoto) {
                                                db.collection("users").document(user.getUid()).update(userMap);

                                                db.collection("artists")
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                                        Log.d("123", document.getId() + " => " + document.getData());
                                                                        if (document.getString("user_id").equals(user.getUid())) {
                                                                            db.collection("artists").document(document.getId()).update(artistMap);
                                                                        }

                                                                    }
                                                                } else {
                                                                    Log.w("123", "Error getting documents.", task.getException());
                                                                }
                                                            }
                                                        });

                                                Toast.makeText(getApplicationContext(), "Update Successful!", Toast.LENGTH_LONG).show();

                                                if (changePhoto) {
                                                    uploadPhoto();
                                                } else {
                                                    leaveNow();
                                                }

                                            }
                                        }

                                    }
                                });

                                findViewById(R.id.view_artist_profile).setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {

                                        db.collection("artists")
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            for (QueryDocumentSnapshot document : task.getResult()) {

                                                                if (document.getString("user_id").equals(user.getUid())) {

                                                                    Intent intent = new Intent(EditAccountActivity.this, ArtistProfileActivity.class);
                                                                    intent.putExtra("ARTIST_DOCUMENT_ID", document.getId());
                                                                    startActivity(intent);
                                                                    finish();
                                                                }

                                                            }
                                                        } else {
                                                            Log.w("123", "Error getting documents.", task.getException());
                                                        }
                                                    }
                                                });
                                    }
                                });

// USER
                            } else {
                                setContentView(R.layout.edit_user_account);


                                findViewById(R.id.request_artist).setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {

                                        Intent myIntent = new Intent(EditAccountActivity.this, ArtistAccountRequestActivity.class);
                                        startActivity(myIntent);
                                        finish();
                                    }
                                });

                                findViewById(R.id.terminate).setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        terminateAccount(userDoc);
                                    }
                                });

                                findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {

                                        Map<String, Object> userMap = new HashMap<>();
                                        EditText username = findViewById(R.id.username);
                                        EditText oldPassword = findViewById(R.id.old_password);
                                        final EditText newPassword = findViewById(R.id.new_password);
                                        final boolean[] changedSomething = {false};

                                        if (!username.getText().toString().equals("")) {
                                            userMap.put("username", username.getText().toString());
                                            changedSomething[0] = true;
                                        }
                                        if (!oldPassword.getText().toString().equals("") && !newPassword.getText().toString().equals("")) {

                                            AuthCredential credential = EmailAuthProvider
                                                    .getCredential(user.getEmail(), oldPassword.getText().toString());

                                            user.reauthenticate(credential)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                user.updatePassword(newPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            Log.d("123", "Password updated");
                                                                            changedSomething[0] = true;
                                                                        } else {
                                                                            Log.d("123", "Error password not updated");
                                                                        }
                                                                    }
                                                                });
                                                            } else {
                                                                Log.d("123", "Error auth failed");
                                                                Toast.makeText(getApplicationContext(), "Wrong Password!", Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });
                                        }

                                        if (changedSomething[0]) {
                                            db.collection("users").document(user.getUid()).update(userMap);
                                            Toast.makeText(getApplicationContext(), "Update Successful!", Toast.LENGTH_LONG).show();
                                            leaveNow();
                                        }
                                    }
                                });
                            }

                        } else {
                            Log.w("123", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void terminateAccount(final DocumentSnapshot[] userDoc) {

        final String[] m_Text = {""};

        AlertDialog.Builder builder = new AlertDialog.Builder(EditAccountActivity.this);
        builder.setTitle("WARNING!");
        builder.setMessage("\nThis action will actually TERMINATE your account! This is PERMANENT. Are you sure? \n\n\nConfirm with Password: \n");
        final EditText input = new EditText(EditAccountActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text[0] = input.getText().toString();

                if (!m_Text[0].isEmpty()) {
                    final AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), m_Text[0]);

                    // Prompt the user to re-provide their sign-in credentials
                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        if (userDoc[0].getBoolean("is_artist")) {
                                            deactivateArtistAccountFirst(credential);
                                            deleteUser();
                                        } else {
                                            deleteUser();
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Wrong Password!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    private void deactivateArtistAccountFirst(AuthCredential credential) {
        final Map<String, Object> userMap = new HashMap<>();
        userMap.put("is_artist", false);

        db.collection("artists").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (final QueryDocumentSnapshot document : task.getResult()) {

                        if (document.getString("user_id").equals(user.getUid())) {
                            artistDoc[0] = document;

                            if (document.getString("profile_image_url").equals(uknownArtistURL)) {
                                deleteArtist_NOTImage(userMap);
                            } else {
                                deleteArtist_ANDImage(userMap, document);
                            }
                        }
                    }
                } else {
                    Log.w("123", "Error getting documents.", task.getException());
                }
            }
        });

    }

    private void deleteUser() {

        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            db.collection("users").document(user.getUid())
                                    .delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                            }
                                        }
                                    });

                            Toast.makeText(getApplicationContext(), "Account Deleted!", Toast.LENGTH_LONG).show();
                            leaveNow();
                        }
                    }
                });
    }

    private void deactivateArtistAccount() {

        final Map<String, Object> userMap = new HashMap<>();
        userMap.put("is_artist", false);
        final String[] m_Text = {""};

        AlertDialog.Builder builder = new AlertDialog.Builder(EditAccountActivity.this);
        builder.setTitle("WARNING!");
        builder.setMessage("\nThis action will actually DEACTIVATE your account! Your Artist Account will be deleted. This is PERMANENT. Are you sure? \n\n\nConfirm with Password: \n");
        final EditText input = new EditText(EditAccountActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text[0] = input.getText().toString();

                if (!m_Text[0].isEmpty()) {
                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), m_Text[0]);

                    // Prompt the user to re-provide their sign-in credentials
                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                db.collection("artists").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (final QueryDocumentSnapshot document : task.getResult()) {

                                                if (document.getString("user_id").equals(user.getUid())) {
                                                    artistDoc[0] = document;

                                                    if (document.getString("profile_image_url").equals(uknownArtistURL)) {
                                                        deleteArtist_NOTImage(userMap);
                                                    } else {
                                                        deleteArtist_ANDImage(userMap, document);
                                                    }
                                                }
                                            }
                                        } else {
                                            Log.w("123", "Error getting documents.", task.getException());
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(getApplicationContext(), "Wrong Password!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void deleteArtist_ANDImage(final Map<String, Object> userMap, QueryDocumentSnapshot document) {
        StorageReference desertRef = mFirebaseStorage.getReferenceFromUrl(document.getString("profile_image_url"));

        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                db.collection("artists").document(artistDoc[0].getId())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                db.collection("users").document(user.getUid()).update(userMap);
                                Log.d("123", "Account Deactivated Successfully!");
                                Toast.makeText(getApplicationContext(), "Account Deactivated Successfully!", Toast.LENGTH_LONG).show();
                                leaveNow();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("123", "Error!!", e);
                                Toast.makeText(getApplicationContext(), "Error!!", Toast.LENGTH_LONG).show();
                                leaveNow();
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
    }

    private void deleteArtist_NOTImage(final Map<String, Object> userMap) {
        db.collection("artists").document(artistDoc[0].getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        db.collection("users").document(user.getUid()).update(userMap);
                        Log.d("123", "Account Deactivated Successfully!");
                        Toast.makeText(getApplicationContext(), "Account Deactivated Successfully!", Toast.LENGTH_LONG).show();
                        leaveNow();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("123", "Error!!", e);
                        Toast.makeText(getApplicationContext(), "Error!!", Toast.LENGTH_LONG).show();
                        leaveNow();
                    }
                });
    }

    private void showProfileImage(final CircleImageView profileImage, final ProgressBar imageProg) {
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
    }

    private void changeGenre(final List<String> artist_type) {
        fStore.collection("artist_type")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            artist_type.add("Choose a Genre...");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                artist_type.add(document.getId() + "");

                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditAccountActivity.this, android.R.layout.simple_spinner_item, artist_type);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            sItems = (Spinner) findViewById(R.id.genre);
                            sItems.setAdapter(adapter);
                        } else {
                            Log.d("123", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    public void uploadPhoto() {

        if (filePath != null) {
            final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            final ProgressBar progress = findViewById(R.id.progressBar);
            final TextView percentage = findViewById(R.id.perc);
            percentage.setVisibility(View.VISIBLE);
            progress.setVisibility(View.VISIBLE);
            percentage.setText("Uploading...");
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            final StorageReference riversRef = storageReference.child("profiles/profile" + user.getUid() + new Date().getTime() + ".jpg");


            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            percentage.setVisibility(View.INVISIBLE);
                            progress.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "File Uploaded", Toast.LENGTH_LONG).show();

                            leaveNow();
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

                            percentage.setText("Waiting to finish...");
                            percentage.setVisibility(View.VISIBLE);
                            progress.setVisibility(View.VISIBLE);
                            photoPath = String.valueOf(downloadUri);

                            db.collection("artists")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (final QueryDocumentSnapshot document : task.getResult()) {
                                                    Log.d("123", document.getId() + " => " + document.getData());
                                                    if (document.getString("user_id").equals(user.getUid())) {

                                                        StorageReference desertRef = mFirebaseStorage.getReferenceFromUrl(document.getString("profile_image_url"));

                                                        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Map<String, Object> artistMap = new HashMap<>();
                                                                artistMap.put("profile_image_url", photoPath);
                                                                db.collection("artists").document(document.getId()).update(artistMap);
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception exception) {
                                                                // Uh-oh, an error occurred!
                                                            }
                                                        });
                                                    }
                                                }
                                            } else {
                                                Log.w("123", "Error getting documents.", task.getException());
                                            }
                                        }
                                    });
                        }

                    }
                }
            });


            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                CircleImageView image = findViewById(R.id.artist_image);
                image.setImageBitmap(bitmap);
                changePhoto = true;
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

    public void leaveNow() {
        Intent myIntent = new Intent(EditAccountActivity.this, HomePage.class);
        startActivity(myIntent);
        finish();
    }


}
