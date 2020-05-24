package com.aueb.urbanarts;

import android.Manifest;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

import de.hdodenhof.circleimageview.CircleImageView;

import static android.widget.Toast.LENGTH_LONG;

public class HomePage extends AppCompatActivity {
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    final String TAG = "123";
    String tempStr = "";
    TextView temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Intent intent=getIntent();
        if (intent.getStringExtra("NoEvent") != null) {
            Toast toast=Toast.makeText(HomePage.this,"Event is no longer Available",LENGTH_LONG);
            toast.show();
        }

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            final ProgressBar loadingImage = findViewById(R.id.loading_image);
            loadingImage.setVisibility(View.VISIBLE);
            showUserInfo(loadingImage);
        } else {
            final ProgressBar loadingImage = findViewById(R.id.loading_image);
            loadingImage.setVisibility(View.INVISIBLE);
        }

        ImageView loginImg = findViewById(R.id.logIn);
        CardView login = findViewById(R.id.cardView_login);
        if (mAuth.getCurrentUser() != null) {
            loginImg.setImageResource(R.drawable.log_out);
            tempStr = "Log Out";
            temp = findViewById(R.id.tv_logIn);
            temp.setText(tempStr);
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuth.signOut();
                    Intent myIntent = new Intent(HomePage.this, HomePage.class);
                    startActivity(myIntent);
                    Animatoo.animateZoom(HomePage.this);
                }
            });
        } else {
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(HomePage.this, LogIn.class);
                    startActivity(myIntent);
                    Animatoo.animateFade(HomePage.this);
                }
            });
        }

        ImageView registerImg = findViewById(R.id.register);
        CardView register = findViewById(R.id.cardView_register);
        if (mAuth.getCurrentUser() != null) {
            registerImg.setImageResource(R.drawable.edit_account);
            tempStr = "Edit Account";
            temp = findViewById(R.id.tv_register);
            temp.setText(tempStr);
            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goEditAccount();
                }
            });
        } else {
            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(HomePage.this, SignUp.class);
                    startActivity(myIntent);
                    Animatoo.animateFade(HomePage.this);
                }
            });
        }

        findViewById(R.id.account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null) {
                    DocumentReference docUser = db.collection("users").document(mAuth.getUid());
                    docUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    if (document.getBoolean("is_artist")) {
                                        Intent myIntent = new Intent(HomePage.this, ArtistProfileActivity.class);
                                        myIntent.putExtra("ARTIST_DOCUMENT_ID", mAuth.getUid());
                                        startActivity(myIntent);
                                        Animatoo.animateFade(HomePage.this);
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

        CardView searchEvents = findViewById(R.id.cardView_events);
        searchEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HomePage.this, SearchFilters.class);
                startActivity(myIntent);
                Animatoo.animateFade(HomePage.this);
            }
        });

        CardView searchArtists = findViewById(R.id.cardView_artists);
        searchArtists.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(HomePage.this, SearchArtists.class);
                startActivity(intent);
                Animatoo.animateFade(HomePage.this);
            }
        });

        final CardView eventFeed = findViewById(R.id.cardView_feed);
        eventFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(HomePage.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    eventFeed.performClick();
                } else {
                    Intent myIntent = new Intent(HomePage.this, Feed.class);
                    startActivity(myIntent);
                    Animatoo.animateFade(HomePage.this);
                }
            }
        });

        CardView favorites = findViewById(R.id.cardView_following);
        favorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null) {
                    Intent intent = new Intent(HomePage.this, Favorites.class);
                    startActivity(intent);
                    Animatoo.animateFade(HomePage.this);
                } else {
                    openDialog();
                }
            }
        });

        CardView createPost = findViewById(R.id.cardView_post);
        createPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null) {
                    Intent myIntent = new Intent(HomePage.this, PostSomething.class);
                    startActivity(myIntent);
                    Animatoo.animateFade(HomePage.this);
                } else {
                    openDialog();
                }
            }
        });

        CardView mapActivity = findViewById(R.id.cardView_map);
        mapActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, ShowMapActivity.class);
                startActivity(intent);
                Animatoo.animateFade(HomePage.this);
            }
        });
    }

    private void showUserInfo(final ProgressBar loadingImage) {
        DocumentReference docUser = db.collection("users").document(mAuth.getUid());
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

                            DocumentReference docArtist = db.collection("artists").document(mAuth.getUid());
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
                Intent myIntent = new Intent(HomePage.this, LogIn.class);
                startActivity(myIntent);
                Animatoo.animateFade(HomePage.this);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                Intent myIntent = new Intent(HomePage.this, SignUp.class);
                startActivity(myIntent);
                Animatoo.animateFade(HomePage.this);
            }
        });
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    private void goEditAccount() {
        Intent intent = new Intent(this, EditAccountActivity.class);
        startActivity(intent);
        Animatoo.animateFade(this);
    }
}