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
    int[] artistGallery = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artist_profile);

        Intent intent = getIntent();
        String artist_id = intent.getStringExtra("ARTIST_DOCUMENT_ID");

        DocumentReference artistDoc = db.collection("artists").document(artist_id);
        ProgressBar imageProg = findViewById(R.id.progress_bar);
        imageProg.setVisibility(View.VISIBLE);
        CircleImageView profileImage = findViewById(R.id.artist_profile_photo);
        TextView artistName = findViewById(R.id.artist_name);
        TextView genre_type = findViewById(R.id.artist_genre);
        TextView year = findViewById(R.id.artist_age);
        TextView description = findViewById(R.id.artist_description);
        TextView followers = findViewById(R.id.num_follows);

        showProfileImage(artistDoc, imageProg, profileImage);
        showArtistName(artistDoc, artistName);
        showGenre(artistDoc, genre_type);
        showYear(artistDoc, year);
        showDescription(artistDoc, description);
        showFollowers(artistDoc, followers);

        artistGallery = getGallery(artist_id);

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

    private void showFollowers(DocumentReference artistDoc, final TextView followers) {
        artistDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    String foll = String.valueOf(document.get("followers"));

                    followers.setText(foll);
                } else {
                    Log.d(TAG, "get failed with ", task.getException());

                }
            }
        });
    }

    private void showDescription(DocumentReference artistDoc, final TextView description) {

        artistDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    String desc = String.valueOf(document.get("description"));

                    description.setText(desc);
                } else {
                    Log.d(TAG, "get failed with ", task.getException());

                }
            }
        });
    }

    private void showYear(DocumentReference artistDoc, final TextView year) {
        artistDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    String whatYear = String.valueOf(document.get("year"));
                    String type = String.valueOf(document.get("artist_type"));
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
                        } else if (findAge == 1){
                            whatToSay = "1 year together.";
                        }
                    }

                    year.setText(whatToSay);
                } else {
                    Log.d(TAG, "get failed with ", task.getException());

                }
            }
        });
    }

    private void showGenre(DocumentReference artistDoc, final TextView genre_type) {

        artistDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    String genre = String.valueOf(document.get("genre"));

                    genre_type.setText(genre);
                } else {
                    Log.d(TAG, "get failed with ", task.getException());

                }
            }
        });
    }

    private void showArtistName(DocumentReference artistDoc, final TextView artistName) {

        artistDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    String artist_name = String.valueOf(document.get("display_name"));

                    artistName.setText(artist_name);
                } else {
                    Log.d(TAG, "get failed with ", task.getException());

                }
            }
        });
    }

    private void showProfileImage(DocumentReference artistDoc, final ProgressBar imageProg, final CircleImageView profileImage) {

        artistDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    String imageURL = String.valueOf(document.get("profile_image_url"));

                    Picasso.with(getApplicationContext()).load(imageURL).into(profileImage, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            imageProg.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onError() {

                        }
                    });

                } else {
                    Log.d(TAG, "get failed with ", task.getException());

                }
            }
        });
    }

    private int[] getGallery(String artist_id) {
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
