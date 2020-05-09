package com.aueb.urbanarts;


import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;


public class ShowMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        boolean hide = googleMap.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.hide_stores)));

        map = googleMap;

        int height = 90;
        int width = 90;
        BitmapDrawable marrkerImage = (BitmapDrawable) getResources().getDrawable(R.drawable.marker);
        Bitmap b = marrkerImage.getBitmap();
        Bitmap smallerMarker = Bitmap.createScaledBitmap(b, width, height, false);

        LatLng position = new LatLng(37.984513, 23.727946);
        map.addMarker(
                new MarkerOptions()
                        .position(position)
                        .icon(BitmapDescriptorFactory.fromBitmap(smallerMarker)));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15.0f));
    }
}
