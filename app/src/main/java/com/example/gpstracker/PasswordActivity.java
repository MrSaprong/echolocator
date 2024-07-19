package com.example.gpstracker;

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

public class PasswordActivity extends AppCompatActivity {

    private String email;
    private EditText e3_password;
    private TextView passwordStrengthTextView;
    private ImageView passwordToggle;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        e3_password = findViewById(R.id.editTextTextPassword2);
        passwordStrengthTextView = findViewById(R.id.password_strength_textview);
        passwordToggle = findViewById(R.id.password_toggle);

        Intent myIntent = getIntent();
        if (myIntent != null) {
            email = myIntent.getStringExtra("email");
        }

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "No email provided", Toast.LENGTH_SHORT).show();
        }

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

        passwordToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });
    }

    public void goToNamePicActivity(View v) {
        String password = e3_password.getText().toString();
        if (isPasswordValid(password)) {
            Intent myIntent = new Intent(PasswordActivity.this, NameActivity.class);
            myIntent.putExtra(getString(R.string.email), email);
            myIntent.putExtra(getString(R.string.password), password);
            startActivity(myIntent);
        } else {
            Toast.makeText(getApplicationContext(), "Password must be more than six(6) characters and contain at least one special character", Toast.LENGTH_LONG).show();
        }
    }

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

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            e3_password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordToggle.setImageResource(R.drawable.ic_visibility_off);
        } else {
            e3_password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordToggle.setImageResource(R.drawable.ic_visibility);
        }
        e3_password.setSelection(e3_password.length());
        isPasswordVisible = !isPasswordVisible;
    }
}
