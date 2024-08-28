package com.example.gpstracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class NameActivity extends AppCompatActivity {

    private String email, password;
    private EditText editTextName;
    private CircleImageView circleImageView;
    private Uri resultUri;
    private CheckBox emailVerificationCheckbox;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private Handler handler;
    private Runnable verificationCheck;
    private DatabaseReference reference;
    private ProgressDialog progressDialog;
    private Button checkVerificationButton;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        editTextName = findViewById(R.id.editTextText);
        circleImageView = findViewById(R.id.circleImageView);
        emailVerificationCheckbox = findViewById(R.id.email_verification_checkbox);
        checkVerificationButton = findViewById(R.id.check_verification_button); // Assume this button is in the layout
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        progressDialog = new ProgressDialog(this);
        storageReference = FirebaseStorage.getInstance().getReference().child("ProfileImages");

        Intent myIntent = getIntent();
        if (myIntent != null) {
            email = myIntent.getStringExtra("email");
            password = myIntent.getStringExtra("password");
        }

        user = auth.getCurrentUser();
        handler = new Handler();

        checkVerificationButton.setOnClickListener(v -> checkEmailVerification());

        if (user != null && !user.isEmailVerified()) {
            handler.post(verificationCheck);
        }
    }

    // Method to check email verification status
    private void checkEmailVerification() {
        user.reload().addOnCompleteListener(task -> {
            if (user != null && user.isEmailVerified()) {
                emailVerificationCheckbox.setVisibility(View.VISIBLE);
                emailVerificationCheckbox.setChecked(true);
                Toast.makeText(NameActivity.this, "Email verified. You can now proceed.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(NameActivity.this, "Email not verified. Please verify your email before proceeding.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void generateCode(View v) {
        String name = editTextName.getText().toString();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (resultUri == null) {
            Toast.makeText(this, "Please select a profile picture.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (user != null && user.isEmailVerified()) {
            progressDialog.setMessage("Uploading image...");
            progressDialog.show();

            // Create a unique file name for the image
            String fileName = user.getUid() + "_" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storageReference.child(fileName);

            // Upload the file to Firebase Storage
            UploadTask uploadTask = imageRef.putFile(resultUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Get the download URL of the uploaded image
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();

                    // Proceed to the next activity with the image URL
                    Intent intent = new Intent(NameActivity.this, InviteCodeActivity.class);
                    intent.putExtra("name", name);
                    intent.putExtra("email", email);
                    intent.putExtra("password", password);
                    intent.putExtra("imageUrl", imageUrl); // Pass the image URL to the next activity

                    Date myDate = new Date();
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String date = format.format(myDate);
                    Random r = new Random();
                    int n = 100000 + r.nextInt(900000);
                    String code = String.valueOf(n);
                    String isSharing = "false";

                    intent.putExtra("code", code);
                    intent.putExtra("isSharing", isSharing);

                    progressDialog.dismiss();
                    startActivity(intent);
                    finish();

                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(NameActivity.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                });

            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(NameActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(NameActivity.this, "Please verify your email before generating a code.", Toast.LENGTH_SHORT).show();
        }
    }

    public void selectImage(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                Uri sourceUri = data.getData();
                if (sourceUri != null) {
                    String destinationFileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    destinationFileName += ".jpg";
                    Uri destinationUri = Uri.fromFile(new File(getCacheDir(), destinationFileName));
                    UCrop.of(sourceUri, destinationUri).start(this);
                }
            }
        }

        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            if (data != null) {
                resultUri = UCrop.getOutput(data);
                if (resultUri != null) {
                    circleImageView.setImageURI(resultUri);
                    Log.d("NameActivity", "Image URI: " + resultUri.toString());
                } else {
                    Log.d("NameActivity", "Cropped Image URI is null");
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(verificationCheck);
    }
}
