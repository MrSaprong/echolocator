package com.example.gpstracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

public class RegisterActivity extends AppCompatActivity {

    private EditText e4_email;
    private FirebaseAuth auth;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        e4_email = findViewById(R.id.editTextTextEmailAddress4);
        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);
    }

    public void goToPasswordActivity(View v) {
        String email = e4_email.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getApplicationContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter an email", Toast.LENGTH_SHORT).show();
            return;
        }

        dialog.setMessage("Checking email address");
        dialog.show();

        // Check if email is already registered or not
        auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        dialog.dismiss();
                        if (task.isSuccessful()) {
                            boolean check = !task.getResult().getSignInMethods().isEmpty();
                            if (!check) {
                                // Email does not exist, so we can create this email with the user
                                Toast.makeText(getApplicationContext(), "Email is available", Toast.LENGTH_SHORT).show();
                                // Proceed to password activity
                                Intent myIntent = new Intent(RegisterActivity.this, PasswordActivity.class);
                                myIntent.putExtra("email", email);
                                startActivity(myIntent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "This email already exists", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle error
                            Toast.makeText(getApplicationContext(), "Error checking email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
