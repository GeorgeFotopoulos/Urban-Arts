package com.aueb.urbanarts;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class ArtistProfileActivity extends AppCompatActivity {

    final String TAG = "123";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CarouselView carouselView;
    int[] artistImages = {R.drawable.artist_image_1, R.drawable.artist_image_2, R.drawable.artist_image_3};
    //    I know Fotaki
    String artistName;
    String artistType;
    String artistGenre;
    String artistYear;
    String followersNum;
    String artistDescription;
    String artistImage;
    int[] artistGallery = {};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artist_profile);

        ProgressBar imageProg = findViewById(R.id.progress_bar);
        imageProg.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        String artist_id = intent.getStringExtra("ARTIST_DOCUMENT_ID");

        getArtistInformation(artist_id);

        findViewById(R.id.report).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ArtistProfileActivity.this, ReportUser.class);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.home_button).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ArtistProfileActivity.this, HomePage.class);
                startActivity(intent);
                finish();
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

        carouselView = findViewById(R.id.gallery);
        carouselView.setPageCount(artistGallery.length);
        carouselView.setImageListener(imageListener);

        ImageView report = (ImageView) findViewById(R.id.report);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(ArtistProfileActivity.this, ReportUser.class);
                ArtistProfileActivity.this.startActivity(myIntent);
            }
        });
    }

    private void getArtistInformation(String artist_id) {
        final CircleImageView profileImage = findViewById(R.id.artist_profile_photo);
        final TextView artistNameDisplay = findViewById(R.id.artist_name);
        final TextView genreDisplay = findViewById(R.id.artist_genre);
        final TextView yearDisplay = findViewById(R.id.artist_age);
        final TextView descriptionDisplay = findViewById(R.id.artist_description);
        final TextView followersNumDisplay = findViewById(R.id.num_follows);

        DocumentReference docArtist = db.collection("artists").document(artist_id);
        docArtist.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());


                        artistName = document.getString("display_name");
                        artistType = document.getString("artist_type");
                        artistGenre = document.getString("genre");
                        artistYear = document.getString("year");
                        artistDescription = document.getString("description");
                        artistName = document.getString("display_name");
                        artistImage = document.getString("profile_image_url");
                        followersNum = document.getString("followers");

                        showProfileImage(profileImage, artistImage);
                        showArtistName(artistNameDisplay, artistName);
                        showGenre(genreDisplay, artistGenre);
                        showYear(yearDisplay, artistYear);
                        showDescription(descriptionDisplay, artistDescription);
                        showFollowers(followersNumDisplay, followersNum);
//                        showGallery(followersNumDisplay, followersNum);

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
            profileImage.setImageResource(R.drawable.uknown_artist);
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

    private int[] showGallery(String artist_id) {
        int[] gallery = {};

        return gallery;
    }

    ImageListener imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            imageView.setImageResource(artistGallery[position]);
        }
    };
}
