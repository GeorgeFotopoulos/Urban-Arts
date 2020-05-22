package com.aueb.urbanarts;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Event extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageView no_image_view;
    String ArtistID, Artist, Location, genre;
    String likes;
    Map<String, Boolean> liked = new HashMap<>();
    CarouselView carouselView;
    TextView LocationTV, ArtistTV, GenreTV, upvotes, upvtext;
    ImageView check;
    List<String> Images = new ArrayList<>();
    List<String> Comments = new ArrayList<>();
    List<String> Users = new ArrayList<>();
    ArrayList<String> UsersAndComments;
    CommentAdapter CommentAdapter;
    private static final int PICK_IMAGE_REQUEST = 22;
    String ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        final String document_id = intent.getStringExtra("eventID");
        ID = document_id;
        setContentView(R.layout.event);
        carouselView = findViewById(R.id.gallery);
        no_image_view = findViewById(R.id.no_image_view);
        carouselView.setPageCount(Images.size());
        carouselView.setImageListener(imageListener);

        if (mAuth.getCurrentUser() != null) {
            ImageView reportEvent = findViewById(R.id.reportEvent);
            reportEvent.setVisibility(View.VISIBLE);
            reportEvent.setClickable(true);
            reportEvent.setFocusable(true);
            reportEvent.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(Event.this, ReportEvent.class);
                    intent.putExtra("event_id", document_id);
                    startActivity(intent);
                    Animatoo.animateFade(Event.this);
                }
            });
        }

        DocumentReference docRef = db.collection("events").document(document_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ImageView LiveImg = findViewById(R.id.live);
                        Boolean liveEvent = document.getBoolean("Live");
                        if (liveEvent) {
                            LiveImg.setVisibility(View.VISIBLE);
                        }
                        ArtistID = document.getString("ArtistID");
                        Artist = document.getString("Artist");
                        Location = document.getString("location");
                        genre = document.getString("genre");
                        LocationTV = findViewById(R.id.eventAddress);
                        ArtistTV = findViewById(R.id.eventArtist);
                        GenreTV = findViewById(R.id.eventGenre);
                        final ImageView Icon = findViewById(R.id.view_artist_profile);
                        try {
                            DocumentReference docRefART = db.collection("artists").document(ArtistID);
                            docRefART.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        final DocumentSnapshot documentArt2 = task.getResult();
                                        if (documentArt2.exists()) {
                                            Icon.setVisibility(View.VISIBLE);
                                            Icon.setClickable(true);
                                            Icon.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent = new Intent(Event.this, ArtistProfileActivity.class);
                                                    intent.putExtra("ARTIST_DOCUMENT_ID", ArtistID);
                                                    Log.d("", ArtistID);
                                                    startActivity(intent);
                                                    Animatoo.animateFade(Event.this);
                                                }
                                            });
                                        }
                                    }
                                }
                            });
                        } catch (Exception e) {

                        }
                        final ConstraintLayout CL = findViewById(R.id.upvotebtn);
                        final Button CommentBtn = findViewById(R.id.btnComment);
                        final ImageView addImage = findViewById(R.id.addImage);


                        upvtext = findViewById(R.id.upvtext);
                        check = findViewById(R.id.check);
                        Images = (List<String>) document.get("gallery");
                        UsersAndComments = (ArrayList<String>) document.get("comments");
                        String[] result;
                        try {
                            for (int i = 0; i < UsersAndComments.size(); i++) {
                                result = UsersAndComments.get(i).split("@token@");
                                Users.add(result[0]);
                                Comments.add(result[1]);
                            }
                        } catch (Exception ignored) {
                        }
                        final RecyclerView recyclerView = findViewById(R.id.CommentRecycler);
                        if (Users.size() == 0) {
                            Users.add(" ");
                            Comments.add("No comments yet.\nBe the first one to comment!");
                            CommentAdapter = new CommentAdapter(Event.this, Users, Comments);
                        } else {
                            CommentAdapter = new CommentAdapter(Event.this, Users, Comments);
                        }
                        recyclerView.setAdapter(CommentAdapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(Event.this));
                        if (!ArtistID.equals("")) {
                            SpannableString content = new SpannableString(Artist);
                            ArtistTV.setText(content);
                            ArtistTV.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DocumentReference docRef2 = db.collection("artists").document(ArtistID);
                                    docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                final DocumentSnapshot documentArt = task.getResult();
                                                if (documentArt.exists()) {
                                                    Intent intent = new Intent(Event.this, ArtistProfileActivity.class);
                                                    intent.putExtra("ARTIST_DOCUMENT_ID", ArtistID);
                                                    Log.d("", ArtistID);
                                                    startActivity(intent);
                                                    Animatoo.animateFade(Event.this);
                                                }
                                            }
                                        }
                                    });

                                }
                            });
                            GenreTV.setText(genre);
                            LocationTV.setText(Location);
                        } else if (Artist.equals("")) {
                            ArtistTV.setText("Unknown Artist");
                            GenreTV.setText(genre);
                            LocationTV.setText(Location);
                        } else {
                            ArtistTV.setText(Artist);
                            GenreTV.setText(genre);
                            LocationTV.setText(Location);
                        }
                        upvotes = findViewById(R.id.upvotes);
                        likes = document.getString("likes");
                        upvotes.setText(likes + " upvotes");
                        if (!Images.isEmpty()) {
                            showGallery(Images);
                            carouselView.setVisibility(View.VISIBLE);
                        } else {
                            no_image_view.setVisibility(View.VISIBLE);
                            findViewById(R.id.load_carousel).setVisibility(View.INVISIBLE);
                        }

                        if (mAuth.getCurrentUser() != null) {
                            addImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showFileChooser();

                                }
                            });
                            DocumentReference docRef2 = db.collection("users").document(mAuth.getCurrentUser().getUid());
                            docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        final DocumentSnapshot document2 = task.getResult();
                                        if (document2.exists()) {
                                            final EditText comment = findViewById(R.id.textComment);
                                            CommentBtn.setOnClickListener(new View.OnClickListener() {

                                                @Override
                                                public void onClick(View v) {
                                                    if (!TextUtils.isEmpty(comment.getText())) {
                                                        if (Users.size() != UsersAndComments.size() & Comments.contains("No comments yet.\nBe the first one to comment!")) {
                                                            Users.remove(0);
                                                            Comments.remove(0);
                                                            recyclerView.removeViewAt(0);
                                                            CommentAdapter.notifyItemRemoved(0);
                                                            CommentAdapter.notifyItemRangeChanged(0, UsersAndComments.size());
                                                            CommentAdapter.notifyDataSetChanged();
                                                        }

                                                        Users.add(document2.get("username") + "");

                                                        Comments.add(comment.getText().toString());
                                                        CommentAdapter = new CommentAdapter(Event.this, Users, Comments);
                                                        DocumentReference docRef4 = db.collection("events").document(document_id);
                                                        docRef4.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    final DocumentSnapshot document4 = task.getResult();
                                                                    if (document4.exists()) {
                                                                        UsersAndComments = (ArrayList<String>) document4.get("comments");
                                                                        UsersAndComments.add(document2.get("username") + "@token@" + Comments.get(Comments.size() - 1));
                                                                        db.collection("events").document(document_id).update("comments", UsersAndComments);
                                                                    }
                                                                }
                                                            }
                                                        });
                                                        recyclerView.setAdapter(CommentAdapter);
                                                        recyclerView.setLayoutManager(new LinearLayoutManager(Event.this));

                                                        Toast.makeText(Event.this, "Comment Made", Toast.LENGTH_SHORT).show();
                                                        try {
                                                            InputMethodManager inputManager = (InputMethodManager) Event.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                                            inputManager.hideSoftInputFromWindow(Event.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                                        } catch (Exception e) {

                                                        }
                                                        comment.setText("");
                                                    } else {
                                                        Toast.makeText(Event.this, "Please add a text to comment first", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                            liked = (Map<String, Boolean>) document2.get("UserLiked");
                                            if (liked == null) {
                                                liked = new HashMap<>();
                                                db.collection("users").document(mAuth.getCurrentUser().getUid()).update("UserLiked", liked);
                                            }
                                            try {
                                                if (liked.containsKey(document_id)) {
                                                    if (liked.get(document_id)) {
                                                        check.setColorFilter(Color.parseColor("#b71e42"));
                                                        upvtext.setText("Upvoted");
                                                    }
                                                }
                                            } catch (Exception ignored) {
                                            }
                                            CL.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if (liked.containsKey(document_id)) {
                                                        if (liked.get(document_id)) {
                                                            check.setColorFilter(Color.parseColor("#7C7C7C"));
                                                            upvtext.setText("Upvote");
                                                            DocumentReference docRef3 = db.collection("events").document(document_id);
                                                            docRef3.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        final DocumentSnapshot document3 = task.getResult();
                                                                        int templikes = Integer.parseInt(document3.getString("likes"));
                                                                        templikes--;

                                                                        liked.put(document_id, false);
                                                                        upvotes.setText(templikes + " upvotes");
                                                                        db.collection("events").document(document_id).update("likes", templikes + "");
                                                                        db.collection("users").document(mAuth.getCurrentUser().getUid()).update("UserLiked", liked);
                                                                    }
                                                                }
                                                            });
                                                        } else {
                                                            check.setColorFilter(Color.parseColor("#b71e42"));
                                                            upvtext.setText("Upvoted");
                                                            DocumentReference docRef3 = db.collection("events").document(document_id);
                                                            docRef3.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        final DocumentSnapshot document3 = task.getResult();
                                                                        int templikes = Integer.parseInt(document3.getString("likes"));
                                                                        templikes++;

                                                                        liked.put(document_id, true);
                                                                        upvotes.setText(templikes + " upvotes");
                                                                        db.collection("events").document(document_id).update("likes", templikes + "");
                                                                        db.collection("users").document(mAuth.getCurrentUser().getUid()).update("UserLiked", liked);
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    } else {
                                                        check.setColorFilter(Color.parseColor("#b71e42"));
                                                        upvtext.setText("Upvoted");
                                                        DocumentReference docRef3 = db.collection("events").document(document_id);
                                                        docRef3.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    final DocumentSnapshot document3 = task.getResult();
                                                                    int templikes = Integer.parseInt(document3.getString("likes"));
                                                                    templikes++;

                                                                    liked.put(document_id, true);
                                                                    upvotes.setText(templikes + " upvotes");
                                                                    db.collection("events").document(document_id).update("likes", templikes + "");
                                                                    db.collection("users").document(mAuth.getCurrentUser().getUid()).update("UserLiked", liked);
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            });
                        } else {
                            addImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    openDialog();

                                }
                            });

                            CL.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    openDialog();
                                }
                            });
                            CommentBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    openDialog();
                                }
                            });

                        }
                    } else {
                        Log.d("", "No such document");
                    }
                } else {
                    Log.d("", "get failed with ", task.getException());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            String filePathStr = filePath.toString();
            if (!filePathStr.isEmpty()) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                ProgressDialog dialog = ProgressDialog.show(Event.this, "",
                        "Uploading photo and loading your post. Please wait...", true);
                // [START upload_create_reference]
                // Create a storage reference from our app
                StorageReference storageRef = storage.getReference();
                Uri file = Uri.parse(filePathStr);
                final StorageReference riversRef = storageRef.child("galleries/" + file.getLastPathSegment());
                UploadTask uploadTask = riversRef.putFile(file);

                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Uri downloadUrl = uri;
                                String url = downloadUrl.toString();
                                Images.add(url);
                                DocumentReference washingtonRef = db.collection("events").document(ID);

                                // Set the "isCapital" field of the city 'DC'
                                washingtonRef
                                        .update("gallery", Images)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                Intent myIntent = new Intent(Event.this, Event.class);
                                                myIntent.putExtra("eventID", ID);
                                                Event.this.startActivity(myIntent);
                                                Animatoo.animateFade(Event.this);
                                                finish();
                                                Log.d("TAG", "DocumentSnapshot successfully updated!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("TAG", "Error updating document", e);
                                            }
                                        });
                            }
                        });
                    }
                });
            } else {
                Intent myIntent = new Intent(Event.this, Event.class);
                myIntent.putExtra("eventID", ID);
                Event.this.startActivity(myIntent);
                Animatoo.animateFade(Event.this);
                finish();
            }
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    protected void openDialog() {
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.login_dialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button cancel = dialog.findViewById(R.id.cancel);
        Button login = dialog.findViewById(R.id.signIn);
        Button register = dialog.findViewById(R.id.signUp);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                Intent myIntent = new Intent(Event.this, LogIn.class);
                Event.this.startActivity(myIntent);
                Animatoo.animateFade(Event.this);
                finish();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                Intent myIntent = new Intent(Event.this, SignUp.class);
                Event.this.startActivity(myIntent);
                Animatoo.animateFade(Event.this);
                finish();
            }
        });
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    private void showGallery(List<String> Images) {
        carouselView.setPageCount(Images.size());
        carouselView.setImageListener(imageListener);
    }

    ImageListener imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            final ProgressBar loadGallery = findViewById(R.id.load_carousel);
            Glide.with(getApplicationContext())
                    .load(Images.get(position))
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
}