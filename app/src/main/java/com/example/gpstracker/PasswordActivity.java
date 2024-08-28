package com.example.gpstracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PasswordActivity extends AppCompatActivity {

    private static final String TAG = "PasswordActivity";
    private String email;
    private EditText e3_password;
    private TextView passwordStrengthTextView;
    private ImageView passwordToggle;
    private boolean isPasswordVisible = false;
    private FirebaseAuth auth;
    private ProgressDialog dialog;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        // Initialize views and Firebase Auth
        e3_password = findViewById(R.id.editTextTextPassword2);
        passwordStrengthTextView = findViewById(R.id.password_strength_textview);
        passwordToggle = findViewById(R.id.password_toggle);
        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);
        nextButton = findViewById(R.id.next_button);

        // Get the email from the previous activity
        Intent myIntent = getIntent();
        if (myIntent != null) {
            email = myIntent.getStringExtra("email");
        }

        // Show a toast message if no email is provided
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "No email provided", Toast.LENGTH_SHORT).show();
            return;
        }

        // Password EditText TextWatcher for strength checking
        e3_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String password = e3_password.getText().toString();
                updatePasswordStrength(password);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Password visibility toggle
        passwordToggle.setOnClickListener(v -> togglePasswordVisibility());

        // Next button logic
        nextButton.setOnClickListener(v -> sendVerificationEmailAndNavigate());
    }

    // Method to send a verification email and navigate to NameActivity
    private void sendVerificationEmailAndNavigate() {
        String password = e3_password.getText().toString();
        if (isPasswordValid(password)) {
            dialog.setMessage("Checking email...");
            dialog.show();

            // Attempt to sign in to check if the email already exists
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(signInTask -> {
                if (signInTask.isSuccessful()) {
                    dialog.dismiss();
                    Toast.makeText(PasswordActivity.this, "The email address is already in use by another account.", Toast.LENGTH_LONG).show();
                } else {
                    // If sign-in fails, the email is likely not registered, proceed with account creation
                    createNewUser(email, password);
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Password must be more than six(6) characters and contain at least one special character", Toast.LENGTH_LONG).show();
        }
    }

    private void createNewUser(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            dialog.dismiss();
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    user.sendEmailVerification().addOnCompleteListener(emailTask -> {
                        if (emailTask.isSuccessful()) {
                            Toast.makeText(PasswordActivity.this, "Verification email sent. Please check your email.", Toast.LENGTH_LONG).show();
                            navigateToNameActivity(password);
                        } else {
                            Toast.makeText(PasswordActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                // Handle specific error if email already exists
                if (task.getException() != null && task.getException().getMessage().contains("The email address is already in use by another account")) {
                    Toast.makeText(PasswordActivity.this, "The email address is already in use by another account.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(PasswordActivity.this, "Failed to create account.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Method to navigate to the NameActivity
    private void navigateToNameActivity(String password) {
        Intent intent = new Intent(PasswordActivity.this, NameActivity.class);
        intent.putExtra("email", email); // Pass email to NameActivity
        intent.putExtra("password", password); // Pass password to NameActivity
        startActivity(intent);
        finish();
    }

    // Method to check if the entered password is valid
    private boolean isPasswordValid(String password) {
        if (password.length() <= 6) {
            return false;
        }
        String specialCharacters = "/*!@#$%^&*()\"{}_[]|\\?/<>,.";
        for (char ch : specialCharacters.toCharArray()) {
            if (password.contains(String.valueOf(ch))) {
                return true;
            }
        }
        return false;
    }

    // Method to determine the strength of the entered password
    private String getPasswordStrength(String password) {
        if (password.length() <= 6) {
            return "Weak";
        }
        String specialCharacters = "/*!@#$%^&*()\"{}_[]|\\?/<>,.";
        boolean hasSpecialChar = false;
        for (char ch : specialCharacters.toCharArray()) {
            if (password.contains(String.valueOf(ch))) {
                hasSpecialChar = true;
                break;
            }
        }
        if (hasSpecialChar && password.length() > 10) {
            return "Strong";
        } else if (hasSpecialChar) {
            return "Medium";
        } else {
            return "Weak";
        }
    }

    // Method to update the password strength TextView based on the password strength
    private void updatePasswordStrength(String password) {
        String strength = getPasswordStrength(password);
        passwordStrengthTextView.setText(strength);
        int color;
        switch (strength) {
            case "Strong":
                color = ContextCompat.getColor(this, android.R.color.holo_green_dark);
                break;
            case "Medium":
                color = ContextCompat.getColor(this, android.R.color.holo_orange_dark);
                break;
            default:
                color = ContextCompat.getColor(this, android.R.color.holo_red_dark);
                break;
        }
        passwordStrengthTextView.setTextColor(color);
    }

    // Method to toggle the visibility of the password in the EditText
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            e3_password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordToggle.setImageResource(R.drawable.ic_visibility);
        } else {
            e3_password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordToggle.setImageResource(R.drawable.ic_visibility_off);
        }
        e3_password.setSelection(e3_password.length());
        isPasswordVisible = !isPasswordVisible;
    }
}
