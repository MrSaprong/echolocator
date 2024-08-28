package com.example.gpstracker;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView nameTextView, emailTextView, sharingStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize UI components
        profileImageView = findViewById(R.id.profile_image);
        nameTextView = findViewById(R.id.profile_name);
        emailTextView = findViewById(R.id.profile_email);
        sharingStatusTextView = findViewById(R.id.profile_sharing_status);

        // Get data passed from previous activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String name = extras.getString("USER_NAME");
            String email = extras.getString("USER_EMAIL");
            String imageUrl = extras.getString("USER_IMAGE");
            boolean isSharing = extras.getBoolean("USER_SHARING");

            // Set data to UI components
            nameTextView.setText(name);
            emailTextView.setText(email);
            sharingStatusTextView.setText(isSharing ? "Location Sharing: Enabled" : "Location Sharing: Disabled");

            // Load profile image using Picasso
            Picasso.get().load(imageUrl).placeholder(R.drawable.defaultprofile).into(profileImageView);
        }
    }
}
