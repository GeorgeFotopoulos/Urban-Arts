package com.aueb.urbanarts;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomePage extends AppCompatActivity {
    private FirebaseAuth mAuth;
    String tempStr = "";
    TextView temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        mAuth = FirebaseAuth.getInstance();

        final ProgressBar loadingIimage = findViewById(R.id.loading_image);
        loadingIimage.setVisibility(View.INVISIBLE);

        showUserInfo(loadingIimage);

        ImageView loginImg = findViewById(R.id.logIn);
        CardView login = findViewById(R.id.cardView1);
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
                    HomePage.this.startActivity(myIntent);
                }
            });
        } else {
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(HomePage.this, LogIn.class);
                    HomePage.this.startActivity(myIntent);
                }
            });
        }

        ImageView registerImg = findViewById(R.id.register);
        CardView register = findViewById(R.id.cardView2);
        if (mAuth.getCurrentUser() != null) {
            registerImg.setImageResource(R.drawable.edit_account);
            tempStr = "My Account";
            temp = findViewById(R.id.tv_register);
            temp.setText(tempStr);
            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(HomePage.this, EditAccountActivity.class);
                    startActivity(myIntent);
                }
            });
        } else {
            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(HomePage.this, SignUp.class);
                    HomePage.this.startActivity(myIntent);
                }
            });
        }

        CardView filters = findViewById(R.id.cardView3);
        filters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HomePage.this, SearchFilters.class);
                HomePage.this.startActivity(myIntent);
            }
        });

        CardView live = findViewById(R.id.cardView4);
        live.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HomePage.this, Feed.class);
                HomePage.this.startActivity(myIntent);
            }
        });

        CardView createPost = findViewById(R.id.cardView5);
        createPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null) {
                    Intent myIntent = new Intent(HomePage.this, PostSomething.class);
                    HomePage.this.startActivity(myIntent);
                } else {
                    openDialog();
                }
            }
        });

        CardView favorites = findViewById(R.id.cardView6);
        favorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null) {
                    Intent myIntent = new Intent(HomePage.this, ArtistAccountRequestActivity.class);
                    HomePage.this.startActivity(myIntent);
                } else {
                    openDialog();
                }
            }
        });
    }

    private void showUserInfo(final ProgressBar loadingIimage) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QueryDocumentSnapshot[] user = new QueryDocumentSnapshot[1];
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("123", document.getId() + " => " + document.getData());
                                if (document.getId().equals(mAuth.getUid())) {
                                    user[0] = document;

                                    String userName = document.getString("username");
                                    TextView displayName = findViewById(R.id.username_display);

                                    displayName.setSelected(true);

                                    displayName.setText(userName);

                                }
                            }

                            final CircleImageView accountImage = findViewById(R.id.account);

                            if (user[0].getBoolean("is_artist")) {

                                loadingIimage.setVisibility(View.VISIBLE);
                                db.collection("artists").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                if (document.getString("user_id").equals(mAuth.getUid())) {


                                                    if (!document.getString("profile_image_url").equals("none")) {
                                                        Picasso.with(getApplicationContext()).load(document.getString("profile_image_url")).into(accountImage, new com.squareup.picasso.Callback() {
                                                            @Override
                                                            public void onSuccess() {
                                                                loadingIimage.setVisibility(View.INVISIBLE);
                                                            }

                                                            @Override
                                                            public void onError() {

                                                            }
                                                        });
                                                    } else {
                                                        accountImage.setImageResource(R.drawable.uknown_artist);
                                                    }

                                                } else {
                                                    Log.d("123", "Not the same user!: ", task.getException());
                                                }
                                            }
                                        } else {
                                            Log.d("123", "Error getting documents: ", task.getException());
                                        }
                                    }
                                });
                            } else {
                                accountImage.setImageResource(R.drawable.uknown_artist);
                            }
                        } else {
                            Log.d("123", "Error getting documents: ", task.getException());
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
                HomePage.this.startActivity(myIntent);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                Intent myIntent = new Intent(HomePage.this, SignUp.class);
                HomePage.this.startActivity(myIntent);
            }
        });
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }
}