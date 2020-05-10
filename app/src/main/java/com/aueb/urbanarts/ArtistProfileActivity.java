package com.aueb.urbanarts;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

public class ArtistProfileActivity extends AppCompatActivity {

    CarouselView carouselView;
    int[] artistImages = {R.drawable.artist_image_1, R.drawable.artist_image_2, R.drawable.artist_image_3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artist_profile);

        findViewById(R.id.report).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(ArtistProfileActivity.this, ReportUser.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.home_button).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(ArtistProfileActivity.this, HomePage.class);
                startActivity(intent);
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
        carouselView.setPageCount(artistImages.length);
        carouselView.setImageListener(imageListener);
    }

    ImageListener imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            imageView.setImageResource(artistImages[position]);
        }
    };
}
