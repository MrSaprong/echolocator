package com.example.gpstracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InviteCodeActivity extends AppCompatActivity {

    // Class members for storing user details
    String name, email, password, issharing, code, date;
    Uri imageUri;

    // ProgressDialog to show loading indication
    ProgressDialog progressDialog;

    // UI elements and Firebase references
    TextView t1;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference reference;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_code);

        // Initialize the ProgressDialog
        progressDialog = new ProgressDialog(this);

        // Initialize UI elements
        t1 = findViewById(R.id.textView);

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance();

        // Get data passed from the previous activity
        Intent myIntent = getIntent();

        // Initialize FirebaseDatabase reference
        reference = FirebaseDatabase.getInstance().getReference().child("Users");

        // Check if the intent has extras and retrieve them
        if (myIntent != null) {
            name = myIntent.getStringExtra("name");
            email = myIntent.getStringExtra("email");
            password = myIntent.getStringExtra("password");
            code = myIntent.getStringExtra("code");
            issharing = myIntent.getStringExtra("isSharing");
            imageUri = myIntent.getParcelableExtra("uri");
        }

        // Set the invite code in the TextView
        t1.setText(code);
    }

    // Method to register the user when the register button is clicked
    public void registerUser(View v) {
        // Show the progress dialog
        progressDialog.setMessage("Please wait. Your account is being created");
        progressDialog.show();

        // Create a new user with email and password using FirebaseAuth
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Proceed with creating the user in the database
                            CreateUser createUser = new CreateUser(name, email, password, code, "false", "na", "na", "na");
                            user = auth.getCurrentUser();
                            userId = user != null ? user.getUid() : null;

                            if (userId != null) {
                                reference.child(userId).setValue(createUser)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                progressDialog.dismiss();
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getApplicationContext(), "User Registered Successfully!", Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "User Registration Failed!", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "User Registration Failed: User ID is null", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            // Log the error message
                            progressDialog.dismiss();
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(getApplicationContext(), "User Registration Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }
}
