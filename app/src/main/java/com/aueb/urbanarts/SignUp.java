package com.aueb.urbanarts;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
    public static final String TAG = "TAG";
    TextView tv_username, tv_email, tv_password, tv_login;
    private FirebaseAuth mAuth;
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    Button btn_createAccount;
    String userID;
    boolean correctInput = true;

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
                finish();
            }
        });

        btn_createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = tv_username.getText().toString().trim();
                final String email = tv_email.getText().toString().trim();
                final String password = tv_password.getText().toString().trim();

                if (TextUtils.isEmpty(username)) {
                    tv_username.setError("Username is required.");
                    correctInput = false;
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    tv_email.setError("Email is required.");
                    correctInput = false;
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    tv_password.setError("Password is required.");
                    correctInput = false;
                    return;
                }

                if (password.length() < 8) {
                    tv_password.setError("Password must be >= 8 characters.");
                    correctInput = false;
                    return;
                }

                if(correctInput = true) {
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
                            user.put("is_artist", false);
                            Map<String, Boolean> liked = new HashMap<>();
                            user.put("UserLiked",liked);
                            user.put("followedUsers",liked);



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
}
