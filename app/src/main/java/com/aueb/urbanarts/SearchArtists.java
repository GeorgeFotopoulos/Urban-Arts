package com.aueb.urbanarts;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchArtists extends AppCompatActivity {
    String TAG, location = "", name = "", typeOfArt = "", live = "";
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private List<ExampleItem> AppArtists = new ArrayList<>();
    public static String ID = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_artists_with_filters);
        retrieveList();
        final AutoCompleteTextView tv_name = findViewById(R.id.actv);

        findViewById(R.id.searchSpecificArtist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(tv_name.getText())) {
                    name = tv_name.getText().toString().trim();
                } else {
                    name = "";
                }

                if (!name.contains(" (UA User)")) {
                    ID = "";
                    Toast toast = Toast.makeText(getApplicationContext(), "You should select an Artist from the List", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    DocumentReference docRef2 = fStore.collection("artists").document(ID);
                    docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                final DocumentSnapshot documentArt = task.getResult();
                                if (documentArt.exists()) {
                                    Intent intent = new Intent(SearchArtists.this, ArtistProfileActivity.class);
                                    intent.putExtra("ARTIST_DOCUMENT_ID", ID);
                                    Log.d("", ID);
                                    startActivity(intent);
                                    Animatoo.animateFade(SearchArtists.this);
                                    finish();
                                }else{
                                    Toast toast = Toast.makeText(getApplicationContext(), "Oops. Seems that the Artist is no longer using our App", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }
                        }});
                }
            }
        });
    }


    private void onsuccess() {
        AutoCompleteTextView editText = findViewById(R.id.actv);
        SearchAdapter adapter = new SearchAdapter(this, AppArtists);
        editText.setAdapter(adapter);
    }

    private void retrieveList() {
        final CollectionReference eventsCollection = fStore.collection("artists");
        eventsCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (final QueryDocumentSnapshot document : task.getResult()) {
                        final ExampleItem itemtoadd = new ExampleItem();
                        itemtoadd.setID(document.getId());
                        DocumentReference docRef = fStore.collection("artists").document(document.getId());
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot artistinfo = task.getResult();
                                    if (artistinfo.exists()) {
                                        itemtoadd.setText1(artistinfo.getString("display_name"));
                                        itemtoadd.setImageResource(artistinfo.getString("profile_image_url"));
                                        itemtoadd.setText2(artistinfo.getString("genre"));
                                    }
                                    AppArtists.add(itemtoadd);
                                    onsuccess();
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}
