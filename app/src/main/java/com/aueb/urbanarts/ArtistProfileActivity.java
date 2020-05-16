package com.aueb.urbanarts;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("deprecation")
public class ArtistProfileActivity extends AppCompatActivity {

    final String TAG = "123";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CarouselView carouselView;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    //    I know Fotaki
    String artistName;
    String artistType;
    String artistGenre;
    String artistYear;
    String followersNum;
    String artistDescription;
    String artistImage;
    List<String> artistGallery = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artist_profile);

        ProgressBar imageProg = findViewById(R.id.progress_bar);
        imageProg.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        final String artist_id = intent.getStringExtra("ARTIST_DOCUMENT_ID");
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
                    goReportUser();
                }
            }
        });

        findViewById(R.id.home_button).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goHomePage();
            }
        });

        findViewById(R.id.follow_button).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                if(isFollowed()){
//
//                }
            }
        });

        final ToggleButton follow = (ToggleButton) findViewById(R.id.follow_button);
        follow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    follow.setText("Unfollow");
                } else {
                    follow.setText("Follow");
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

    private void getArtistInformation(String artist_id) {
        final CircleImageView profileImage = findViewById(R.id.artist_profile_photo);
        final TextView artistNameDisplay = findViewById(R.id.artist_name);
        final TextView genreDisplay = findViewById(R.id.artist_genre);
        final TextView yearDisplay = findViewById(R.id.artist_age);
        final TextView descriptionDisplay = findViewById(R.id.artist_description);
        final TextView followersNumDisplay = findViewById(R.id.num_follows);
        final ProgressBar loadGallery = findViewById(R.id.load_carousel);

        DocumentReference docArtist = db.collection("artists").document(artist_id);
        docArtist.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

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
                        } else {
                            noGallery.setText("No Images :(");
                            loadGallery.setVisibility(View.INVISIBLE);
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
            Picasso.with(getApplicationContext()).load(imageURL).into(profileImage, new com.squareup.picasso.Callback() {
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

    private void showGallery(List<String> artistGallery) {
        carouselView.setPageCount(artistGallery.size());
        carouselView.setImageListener(imageListener);
    }

    ImageListener imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {

            final ProgressBar loadGallery = findViewById(R.id.load_carousel);
            Picasso.with(getApplicationContext()).load(artistGallery.get(position)).into(imageView, new com.squareup.picasso.Callback() {
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goHomePage();
    }

    public void goHomePage() {
        startActivity(new Intent(this, HomePage.class));
        Animatoo.animateZoom(this);
        finish();
    }

    private void goEditAccount() {
        startActivity(new Intent(this, EditAccountActivity.class));
        Animatoo.animateFade(this);
        finish();
    }

    private void goReportUser() {
        startActivity(new Intent(this, ReportUser.class));
        Animatoo.animateInAndOut(this);
        finish();
    }
}
