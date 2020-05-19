package com.aueb.urbanarts;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchFilters extends AppCompatActivity {
    String TAG, location = "", name = "", typeOfArt = "", live = "";
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    EditText tv_location, tv_name;
    boolean yesFilter = false;
    Button btnSearch;
    Switch aSwitch;
    Spinner sItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_with_filters);

        final List<String> genres = new ArrayList<>();
        fStore.collection("genre")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            genres.add("Choose...");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                genres.add(document.getId() + "");
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(SearchFilters.this, android.R.layout.simple_spinner_item, genres);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            sItems = findViewById(R.id.genreSpinner);
                            sItems.setAdapter(adapter);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        btnSearch = findViewById(R.id.search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_location = findViewById(R.id.location);
                if (!TextUtils.isEmpty(tv_location.getText())) {
                    location = tv_location.getText().toString().trim();
                    yesFilter = true;
                }

                tv_name = findViewById(R.id.name);
                if (!TextUtils.isEmpty(tv_name.getText())) {
                    name = tv_name.getText().toString().trim();
                    yesFilter = true;
                }

                if (sItems != null) {
                    typeOfArt = sItems.getSelectedItem().toString().trim();
                    if (!typeOfArt.equals("Choose...")) {
                        yesFilter = true;
                    }
                }

                aSwitch = findViewById(R.id.aSwitch);
                if (aSwitch.isChecked()) {
                    live = "true";
                    yesFilter = true;
                } else if (!aSwitch.isChecked()) {
                    live = "false";
                    yesFilter = true;
                }

                if (yesFilter) {
                    Intent intent = new Intent(SearchFilters.this, Feed.class);
                    if (!location.equals("")) intent.putExtra("location", location);
                    if (!name.equals("")) intent.putExtra("name", name);
                    if (!typeOfArt.equals("Choose...")) intent.putExtra("typeOfArt", typeOfArt);
                    if (!live.equals("")) {
                        intent.putExtra("live", live);
                    }
                    startActivity(intent);
                    Animatoo.animateFade(SearchFilters.this);
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "No filters were selected.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SearchFilters.this, HomePage.class);
        startActivity(intent);
        Animatoo.animateZoom(this);
        finish();
    }
}
