package com.example.gpstracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PasswordActivity extends AppCompatActivity {

    // Declare variables for email, EditText for password input, TextView for password strength display, and ImageView for password visibility toggle
    private String email;
    private EditText e3_password;
    private TextView passwordStrengthTextView;
    private ImageView passwordToggle;
    private boolean isPasswordVisible = false;
    private FirebaseAuth auth;
    private ProgressDialog dialog;

    // Method called when the activity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the layout file activity_password.xml
        setContentView(R.layout.activity_password);

        // Initialize EditText, TextView, ImageView, FirebaseAuth, and ProgressDialog from the layout
        e3_password = findViewById(R.id.editTextTextPassword2);
        passwordStrengthTextView = findViewById(R.id.password_strength_textview);
        passwordToggle = findViewById(R.id.password_toggle);
        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);

        // Get the email passed from the previous activity
        Intent myIntent = getIntent();
        if (myIntent != null) {
            email = myIntent.getStringExtra("email");
        }

        // Show a toast message if no email is provided
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "No email provided", Toast.LENGTH_SHORT).show();
        }

        // Add a TextWatcher to the password EditText to monitor changes in the password input
        e3_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String password = e3_password.getText().toString();
                updatePasswordStrength(password); // Update the password strength indicator as the password changes
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Set an OnClickListener on the password visibility toggle ImageView
        passwordToggle.setOnClickListener((View v) -> {
            togglePasswordVisibility(); // Toggle the visibility of the password when the icon is clicked
        });

        // Set an OnClickListener on the check verification button
        findViewById(R.id.check_verification_button).setOnClickListener(v -> {
            checkEmailVerification(); // Check if the email is verified when the button is clicked
        });
    }

    // Method to navigate to the NameActivity when the next button is clicked
    public void goToNamePicActivity(View v) {
        String password = e3_password.getText().toString();
        // Check if the entered password is valid
        if (isPasswordValid(password)) {
            // Show the progress dialog while sending verification email
            dialog.setMessage("Sending verification email...");
            dialog.show();

            // Create user with email and password
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        // Send verification email
                        user.sendEmailVerification().addOnCompleteListener(emailTask -> {
                            if (emailTask.isSuccessful()) {
                                Toast.makeText(PasswordActivity.this, "Verification email sent. Please check your email.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(PasswordActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(PasswordActivity.this, "Failed to create account.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Show a toast message if the password is invalid
            Toast.makeText(getApplicationContext(), "Password must be more than six(6) characters and contain at least one special character", Toast.LENGTH_LONG).show();
        }
    }

    // Method to check if the entered password is valid
    private boolean isPasswordValid(String password) {
        // Password must be longer than 6 characters
        if (password.length() <= 6) {
            return false;
        }
        // Password must contain at least one special character
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
        // Weak if the password is shorter than or equal to 6 characters
        if (password.length() <= 6) {
            return "Weak";
        }
        // Check if the password contains any special characters
        String specialCharacters = "/*!@#$%^&*()\"{}_[]|\\?/<>,.";
        boolean hasSpecialChar = false;
        for (char ch : specialCharacters.toCharArray()) {
            if (password.contains(String.valueOf(ch))) {
                hasSpecialChar = true;
                break;
            }
        }

        // Strong if the password has special characters and is longer than 10 characters
        if (hasSpecialChar && password.length() > 10) {
            return "Strong";
        } else if (hasSpecialChar) {
            // Medium if the password has special characters but is not longer than 10 characters
            return "Medium";
        } else {
            // Weak if the password does not have any special characters
            return "Weak";
        }
    }

    // Method to update the password strength TextView based on the password strength
    private void updatePasswordStrength(String password) {
        String strength = getPasswordStrength(password);
        passwordStrengthTextView.setText(strength);

        // Set the color of the TextView based on the password strength
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
            // If password is currently visible, make it hidden
            e3_password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordToggle.setImageResource(R.drawable.ic_visibility_off); // Change the toggle icon to "visibility off"
        } else {
            // If password is currently hidden, make it visible
            e3_password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordToggle.setImageResource(R.drawable.ic_visibility); // Change the toggle icon to "visibility"
        }
        // Move the cursor to the end of the text
        e3_password.setSelection(e3_password.length());
        isPasswordVisible = !isPasswordVisible; // Toggle the visibility state
    }

    // Method to check if the email is verified
    private void checkEmailVerification() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (user.isEmailVerified()) {
                        // Email is verified, proceed to the next activity
                        Intent myIntent = new Intent(PasswordActivity.this, NameActivity.class);
                        myIntent.putExtra(getString(R.string.email), email);
                        myIntent.putExtra(getString(R.string.password), e3_password.getText().toString());
                        startActivity(myIntent);
                    } else {
                        // Email is not verified
                        Toast.makeText(PasswordActivity.this, "Please verify your email before proceeding.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(PasswordActivity.this, "Failed to reload user.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
