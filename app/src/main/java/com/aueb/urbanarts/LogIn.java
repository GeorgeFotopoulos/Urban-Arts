package com.aueb.urbanarts;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LogIn extends AppCompatActivity {
    TextView mCreateBtn, forgotTextLink, tv_guest;
    EditText mEmail, mPassword;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    Button mLoginBtn;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fAuth = FirebaseAuth.getInstance();
        if (fAuth.getCurrentUser() != null) {
            Intent myIntent = new Intent(LogIn.this, HomePage.class);
            LogIn.this.startActivity(myIntent);
            Animatoo.animateZoom(LogIn.this);
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mLoginBtn = findViewById(R.id.signIn);
        forgotTextLink = findViewById(R.id.forgotPassword);
        mCreateBtn = findViewById(R.id.signUp);
        tv_guest = findViewById(R.id.guest);

        tv_guest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(LogIn.this, HomePage.class);
                startActivity(myIntent);
                Animatoo.animateZoom(LogIn.this);
                finish();
            }
        });

        TextView appName = findViewById(R.id.appName);
        appName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogIn.this, HomePage.class);
                startActivity(intent);
                Animatoo.animateZoom(LogIn.this);
                finish();
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString().trim();
                final String password = mPassword.getText().toString().trim();
                progressBar = findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);

                if (TextUtils.isEmpty(email)) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(LogIn.this, "Email is required!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(LogIn.this, "Password is required!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 8) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(LogIn.this, "Password must be >= 8 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mLoginBtn.setEnabled(false);
                            userID = fAuth.getCurrentUser().getUid();
                            progressBar = findViewById(R.id.progressBar);
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(LogIn.this, "Logged in successfully!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), HomePage.class));
                            Animatoo.animateZoom(LogIn.this);
                            finish();
                        } else {
                            Toast.makeText(LogIn.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                            mLoginBtn.setEnabled(true);
                        }
                    }
                });
            }
        });

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignUp.class));
                Animatoo.animateFade(LogIn.this);
                finish();
            }
        });

        forgotTextLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText resetMail = new EditText(v.getContext());
                final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password");
                passwordResetDialog.setMessage("Enter your email to receive reset link:");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // extract the email and send reset link
                        String mail = resetMail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(LogIn.this, "Reset link was sent to your email.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LogIn.this, "Error! Reset link was not sent." + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // close the dialog
                    }
                });
                passwordResetDialog.create().show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(LogIn.this, HomePage.class);
        startActivity(intent);
        Animatoo.animateZoom(this);
        finish();
    }
}