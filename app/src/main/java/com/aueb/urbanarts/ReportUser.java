package com.aueb.urbanarts;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.textfield.TextInputEditText;

public class ReportUser extends AppCompatActivity {
    TextInputEditText fullNameInput, textInput;
    String text, rg_value, artistID;
    RadioGroup rg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_user);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Intent intent = getIntent();
        artistID = intent.getStringExtra("artist_id");

        rg = findViewById(R.id.radioGroup);
        rg_value = ((RadioButton) findViewById(rg.getCheckedRadioButtonId())).getText().toString();

        Button reportButton = findViewById(R.id.reportButton);
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullNameInput = findViewById(R.id.fullNameInput);
                String fullName = fullNameInput.getText().toString();

                textInput = findViewById(R.id.textInput);
                text = "Name: " + fullName + "\nArtist ID: " + artistID + "\n\n" + textInput.getText().toString();

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"giorgos.fotopoulos7@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, rg_value);
                intent.putExtra(Intent.EXTRA_TEXT, text);
                intent.setType("message/rfc822");
                startActivity(Intent.createChooser(intent, "Sending the email..."));
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ReportUser.this, HomePage.class);
        startActivity(intent);
        Animatoo.animateZoom(this);
        finish();
    }
}