package com.aueb.urbanarts;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    TextView tv_username, tv_email, tv_password, tv_login;
    public static final String TAG = "TAG";
    boolean correctInput = true;
    private FirebaseAuth mAuth;
    Button btn_createAccount;
    ProgressBar progressBar;
    CheckBox mCheckBox;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        tv_username = findViewById(R.id.username);
        tv_email = findViewById(R.id.email);
        tv_password = findViewById(R.id.password);
        tv_login = findViewById(R.id.logIn);
        btn_createAccount = findViewById(R.id.createAccount);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), HomePage.class));
            finish();
        }

        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(SignUp.this, LogIn.class);
                SignUp.this.startActivity(myIntent);
                Animatoo.animateFade(SignUp.this);
                finish();
            }
        });

        btn_createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = tv_username.getText().toString().trim();
                final String email = tv_email.getText().toString().trim();
                final String password = tv_password.getText().toString().trim();
                mCheckBox = findViewById(R.id.checkBox);

                progressBar = findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);

                if (TextUtils.isEmpty(username)) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(SignUp.this, "Username is required!", Toast.LENGTH_SHORT).show();
                    correctInput = false;
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(SignUp.this, "Password is required!", Toast.LENGTH_SHORT).show();
                    correctInput = false;
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(SignUp.this, "Password is required!", Toast.LENGTH_SHORT).show();
                    correctInput = false;
                    return;
                }

                if (password.length() < 8) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(SignUp.this, "Password must be >= 8 characters!", Toast.LENGTH_SHORT).show();
                    correctInput = false;
                    return;
                }

                if (!mCheckBox.isChecked()) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(SignUp.this, "To continue, you must agree to the Urban Arts Terms of Service!", Toast.LENGTH_SHORT).show();
                    correctInput = false;
                    return;
                }

                if (correctInput = true) {
                    btn_createAccount.setEnabled(false);
                }

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // send verification link
                            FirebaseUser fuser = mAuth.getCurrentUser();
                            fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(SignUp.this, "Verification email has been sent.", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: Email not sent " + e.getMessage());
                                }
                            });
                            Toast.makeText(SignUp.this, "User Created.", Toast.LENGTH_SHORT).show();
                            userID = mAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(userID);

                            Map<String, Object> user = new HashMap<>();
                            user.put("user_id", userID);
                            user.put("username", username);
                            user.put("email", email);
                            user.put("followers", "0");
                            user.put("is_artist", false);
                            Map<String, Boolean> liked = new HashMap<>();
                            user.put("UserLiked", liked);
                            user.put("followedUsers", liked);

                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: user Profile is created for " + userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e.toString());
                                }
                            });
                            startActivity(new Intent(getApplicationContext(), HomePage.class));
                            finish();
                        } else {
                            Toast.makeText(SignUp.this, "Error! Could not log in." + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SignUp.this, HomePage.class);
        startActivity(intent);
        Animatoo.animateZoom(this);
        finish();
    }
}
