package com.example.gpstracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InviteCodeActivity extends AppCompatActivity {

    private static final String TAG = "InviteCodeActivity";

    // Variables to store user details
    private String name, email, password, isSharing, code, imageUrl;

    // Progress dialog to show loading indication
    private ProgressDialog progressDialog;

    // UI elements
    private TextView t1;
    private FirebaseAuth auth;
    private DatabaseReference reference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_code);

        // Initialize the ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait. Your account is being created");

        // Find the TextView for displaying the invite code
        t1 = findViewById(R.id.textView);

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance();

        // Initialize Firebase Database reference pointing to the "Users" node
        reference = FirebaseDatabase.getInstance().getReference().child("Users");

        // Get the intent that started this activity
        Intent myIntent = getIntent();

        // Extract data passed from the previous activity
        if (myIntent != null) {
            name = myIntent.getStringExtra("name");
            email = myIntent.getStringExtra("email");
            password = myIntent.getStringExtra("password");
            code = myIntent.getStringExtra("code");
            isSharing = myIntent.getStringExtra("isSharing");
            imageUrl = myIntent.getStringExtra("imageUrl"); // Get the image URL

            // Log the received data
            Log.d(TAG, "Received Data: name=" + name + ", email=" + email + ", password=" + password + ", code=" + code + ", isSharing=" + isSharing + ", imageUrl=" + imageUrl);
        }

        // Set the invite code in the TextView
        t1.setText(code);
    }

    public void registerUser(View v) {
        Log.d(TAG, "Register button clicked");

        // Check if email and password are not null or empty
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            Log.e(TAG, "Email or Password is empty or null");
            Toast.makeText(getApplicationContext(), "Email or Password cannot be empty", Toast.LENGTH_LONG).show();
            return;
        }

        // Show the progress dialog with a message
        progressDialog.show();

        // Try to create a new user with email and password
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registration successful, proceed with saving user data
                            saveUserData();
                        } else {
                            // Check if the failure is due to email already in use
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                // Email already in use, log in the user
                                auth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    // Log in successful, proceed with saving user data
                                                    saveUserData();
                                                } else {
                                                    Log.e(TAG, "User Login Failed!", task.getException());
                                                    Toast.makeText(getApplicationContext(), "User Login Failed!", Toast.LENGTH_LONG).show();
                                                    progressDialog.dismiss();
                                                }
                                            }
                                        });
                            } else {
                                Log.e(TAG, "User Registration Failed!", task.getException());
                                Toast.makeText(getApplicationContext(), "User Registration Failed!", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        }
                    }
                });
    }

    private void saveUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();

            // Create a new CreateUser object with the user details
            CreateUser createUser = new CreateUser(
                    name, email, password, code, "false", "na", "na", imageUrl, currentUser.getUid()
            );

            // Store the user details in the Firebase Database
            reference.child(userId).setValue(createUser)
                    .addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Log.d(TAG, "User Registered Successfully with Image!");
                            Toast.makeText(getApplicationContext(), "User Registered Successfully!", Toast.LENGTH_LONG).show();

                            // Start the next activity
                            Intent myIntent = new Intent(InviteCodeActivity.this, UserLocationMainActivity.class);
                            myIntent.putExtra("imageUrl", imageUrl); // Pass the image URL to the next activity if needed
                            startActivity(myIntent);

                            // Delay finish() to avoid DeadObjectException
                            new Handler().postDelayed(() -> {
                                progressDialog.dismiss(); // Dismiss the progress dialog before finishing the activity
                                finish(); // Close the current activity
                            }, 500);

                        } else {
                            Log.e(TAG, "User Registration Failed!", task1.getException());
                            Toast.makeText(getApplicationContext(), "User Registration Failed!", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    });
        } else {
            Log.e(TAG, "Current user is null after registration or login");
            Toast.makeText(getApplicationContext(), "Error: User registration/login completed, but user is null.", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }
    }
}