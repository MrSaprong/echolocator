package com.example.gpstracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InviteCodeActivity extends AppCompatActivity {

    private static final String TAG = "InviteCodeActivity";

    // Variables to store user details
    String name, email, password, isSharing, code;
    Uri imageUri;

    // Progress dialog to show loading indication
    ProgressDialog progressDialog;

    // UI elements
    TextView t1;
    FirebaseAuth auth;
    DatabaseReference reference;
    String userId;

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
            imageUri = myIntent.getParcelableExtra("uri");

            // Log the received data
            Log.d(TAG, "Received Data: name=" + name + ", email=" + email + ", password=" + password + ", code=" + code + ", isSharing=" + isSharing + ", uri=" + imageUri);
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

        // Get the current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Get the user ID
            userId = currentUser.getUid();

            // Create a new CreateUser object with the user details
            CreateUser createUser = new CreateUser(
                    name, email, password, code, "false", "na", "na",
                    imageUri != null ? imageUri.toString() : "na"
            );

            // Store the user details in the Firebase Database
            reference.child(userId).setValue(createUser)
                    .addOnCompleteListener(task1 -> {
                        // Dismiss the progress dialog
                        progressDialog.dismiss();
                        if (task1.isSuccessful()) {
                            // User registration successful
                            Log.d(TAG, "User Registered Successfully!");
                            Toast.makeText(getApplicationContext(), "User Registered Successfully!", Toast.LENGTH_LONG).show();

                            // Start the MyNavigationActivity
                            Intent myIntent = new Intent(InviteCodeActivity.this, MyNavigationActivity.class);
                            startActivity(myIntent);
                            finish(); // Close the current activity
                        } else {
                            // User registration failed, log the error
                            Log.e(TAG, "User Registration Failed!", task1.getException());
                            Toast.makeText(getApplicationContext(), "User Registration Failed!", Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            // Current user is null, handle the error
            progressDialog.dismiss();
            Log.e(TAG, "No user is currently signed in");
            Toast.makeText(getApplicationContext(), "No user is currently signed in.", Toast.LENGTH_LONG).show();
        }
    }
}
