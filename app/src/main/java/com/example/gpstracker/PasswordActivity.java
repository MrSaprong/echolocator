package com.example.gpstracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PasswordActivity extends AppCompatActivity {

    private String email;
    private EditText e3_password;
    private TextView passwordStrengthTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        e3_password = findViewById(R.id.editTextTextPassword2);
        passwordStrengthTextView = findViewById(R.id.password_strength_textview);

        Intent myIntent = getIntent();
        if (myIntent != null) {
            email = myIntent.getStringExtra("email");
        }

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "No email provided", Toast.LENGTH_SHORT).show();
            // Optionally, you can finish the activity if email is essential
            // finish();
        }

        e3_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String password = e3_password.getText().toString();
                passwordStrengthTextView.setText(getPasswordStrength(password));
            }

            @Override
            public void afterTextChanged(Editable editable) {
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
}
