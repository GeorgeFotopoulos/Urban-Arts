package com.aueb.urbanarts;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Event extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.event);
        carouselView = findViewById(R.id.gallery);
        carouselView.setPageCount(Images.size());
        carouselView.setImageListener(imageListener);
        DocumentReference docRef = db.collection("events").document("pz56iXB5RWdPygrRaoBT");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ImageView LiveImg=findViewById(R.id.live);
                        Boolean liveEvent=document.getBoolean("Live");
                        if(liveEvent){
                            LiveImg.setVisibility(View.VISIBLE);
                        }
                        ArtistID = document.getString("ArtistID");
                        Artist = document.getString("Artist");
                        Location = document.getString("location");
                        genre = document.getString("genre");
                        LocationTV = findViewById(R.id.location);
                        ArtistTV = findViewById(R.id.ArtistName);
                        GenreTV = findViewById(R.id.genre);
                        final ConstraintLayout CL = findViewById(R.id.upvotebtn);
                        final Button CommentBtn = findViewById(R.id.btnComment);
                        upvtext = findViewById(R.id.upvtext);
                        check = findViewById(R.id.check);
                        Images = (List<String>) document.get("gallery");
                        UsersAndComments = (ArrayList<String>) document.get("comments");
                        String[] result = new String[2];
                        for (int i = 0; i < UsersAndComments.size(); i++) {
                            result = UsersAndComments.get(i).split("@token@");
                            Users.add(result[0]);
                            Comments.add(result[1]);
                        }
                        final RecyclerView recyclerView = findViewById(R.id.CommentRecycler);
                        if(Users.size()==0){
                            Users.add(" ");
                            Comments.add("No comments yet.\nBe the first one to comment!");
                            CommentAdapter = new CommentAdapter(Event.this, Users, Comments);
                        }
                        else {
                            CommentAdapter = new CommentAdapter(Event.this, Users, Comments);
                        }
                        recyclerView.setAdapter(CommentAdapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(Event.this));
                        if (ArtistID != "") {
                            SpannableString content = new SpannableString(Artist);
                            content.setSpan(new UnderlineSpan(), 0, Artist.length(), 0);
                            ArtistTV.setText(content);
                            ArtistTV.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Event.this, ArtistProfileActivity.class);
                                    intent.putExtra("ARTIST_DOCUMENT_ID", ArtistID);
                                    Log.d("", ArtistID);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                            GenreTV.setText(genre);
                            LocationTV.setText(Location);
                        } else if (Artist == "") {
                            ArtistTV.setText("");
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
                        }
                        if (mAuth.getCurrentUser() != null) {
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
                                                        if(Users.size()!=UsersAndComments.size()){
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
                                                        DocumentReference docRef4 = db.collection("events").document("pz56iXB5RWdPygrRaoBT");
                                                        docRef4.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    final DocumentSnapshot document4 = task.getResult();
                                                                    if (document4.exists()) {
                                                                        UsersAndComments = (ArrayList<String>) document4.get("comments");
                                                                        UsersAndComments.add(document2.get("username") + "@token@" + Comments.get(Comments.size() - 1));
                                                                        db.collection("events").document("pz56iXB5RWdPygrRaoBT").update("comments", UsersAndComments);
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
                                            if (liked.containsKey("pz56iXB5RWdPygrRaoBT")) {
                                                if (liked.get("pz56iXB5RWdPygrRaoBT") == true) {
                                                    check.setColorFilter(Color.parseColor("#b71e42"));
                                                    upvtext.setText("Upvoted");
                                                }
                                            }
                                            CL.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if (liked.containsKey("pz56iXB5RWdPygrRaoBT")) {
                                                        if (liked.get("pz56iXB5RWdPygrRaoBT") == true) {
                                                            check.setColorFilter(Color.parseColor("#7C7C7C"));
                                                            upvtext.setText("Upvote");
                                                            DocumentReference docRef3 = db.collection("events").document("pz56iXB5RWdPygrRaoBT");
                                                            docRef3.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        final DocumentSnapshot document3 = task.getResult();
                                                                        int templikes = Integer.parseInt(document3.getString("likes"));
                                                                        templikes--;

                                                                        liked.put("pz56iXB5RWdPygrRaoBT", false);
                                                                        upvotes.setText(templikes + " upvotes");
                                                                        db.collection("events").document("pz56iXB5RWdPygrRaoBT").update("likes", templikes + "");
                                                                        db.collection("users").document(mAuth.getCurrentUser().getUid()).update("UserLiked", liked);
                                                                    }
                                                                }
                                                            });
                                                        } else {
                                                            check.setColorFilter(Color.parseColor("#b71e42"));
                                                            upvtext.setText("Upvoted");
                                                            DocumentReference docRef3 = db.collection("events").document("pz56iXB5RWdPygrRaoBT");
                                                            docRef3.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        final DocumentSnapshot document3 = task.getResult();
                                                                        int templikes = Integer.parseInt(document3.getString("likes"));
                                                                        templikes++;

                                                                        liked.put("pz56iXB5RWdPygrRaoBT", true);
                                                                        upvotes.setText(templikes + " upvotes");
                                                                        db.collection("events").document("pz56iXB5RWdPygrRaoBT").update("likes", templikes + "");
                                                                        db.collection("users").document(mAuth.getCurrentUser().getUid()).update("UserLiked", liked);
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    } else {

                                                        check.setColorFilter(Color.parseColor("#b71e42"));
                                                        upvtext.setText("Upvoted");
                                                        DocumentReference docRef3 = db.collection("events").document("pz56iXB5RWdPygrRaoBT");
                                                        docRef3.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    final DocumentSnapshot document3 = task.getResult();
                                                                    int templikes = Integer.parseInt(document3.getString("likes"));
                                                                    templikes++;

                                                                    liked.put("pz56iXB5RWdPygrRaoBT", true);
                                                                    upvotes.setText(templikes + " upvotes");
                                                                    db.collection("events").document("pz56iXB5RWdPygrRaoBT").update("likes", templikes + "");
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
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                Intent myIntent = new Intent(Event.this, SignUp.class);
                Event.this.startActivity(myIntent);
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
//            imageView.setImageResource(artistGallery[position]);
            final ProgressBar loadGallery = findViewById(R.id.load_carousel);
            Picasso.with(getApplicationContext()).load(Images.get(position)).into(imageView, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    loadGallery.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onError() {
                }
            });
        }
    };

}
