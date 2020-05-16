package com.aueb.urbanarts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostSomething extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 22;
    String TAG, name = "", typeOfArt = "", live = "";
    boolean yesFilter = false;
    EditText tv_name;
    Switch aSwitch;
    Spinner sItems;
    Button btnUpload, btnProceed;
    private Uri filePath;
    private Bitmap bitmap;
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.make_post);

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
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(PostSomething.this, android.R.layout.simple_spinner_item, genres);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            sItems = findViewById(R.id.genreSpinner);
                            sItems.setAdapter(adapter);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        btnUpload = findViewById(R.id.uploadFile);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (v == btnUpload) {
                    showFileChooser();
                    yesFilter = true;
                }
            }
        });

        btnProceed = findViewById(R.id.proceed);
        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_name = findViewById(R.id.name);
                if (!TextUtils.isEmpty(tv_name.getText())) {
                    name = tv_name.getText().toString().trim();
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
                } else if (!aSwitch.isChecked()) {
                    live = "false";
                }

                if (yesFilter) {
                    Intent intent = new Intent(PostSomething.this, ShowPostOnMapActivity.class);
                    if (!name.equals("")) intent.putExtra("name", name);
                    if (!typeOfArt.equals("Choose...")) intent.putExtra("typeOfArt", typeOfArt);
                    if (!live.equals("")) {
                        intent.putExtra("live", live);
                    }
                    if (filePath != null) {
                        intent.putExtra("filePath", filePath.toString());
                    }
                    startActivity(intent);
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Fields \"Art Genre\", \"Live\" & \"Upload Media\" are required.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }
}
