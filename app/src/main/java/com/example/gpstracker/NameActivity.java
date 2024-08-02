package com.example.gpstracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        editTextName = findViewById(R.id.editTextText);
        circleImageView = findViewById(R.id.circleImageView);
        emailVerificationCheckbox = findViewById(R.id.email_verification_checkbox);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        progressDialog = new ProgressDialog(this);

        Intent myIntent = getIntent();
        if (myIntent != null) {
            email = myIntent.getStringExtra("email");
            password = myIntent.getStringExtra("password");
        }

        user = auth.getCurrentUser();
        handler = new Handler();
        verificationCheck = new Runnable() {
            @Override
            public void run() {
                user.reload().addOnCompleteListener(task -> {
                    if (user != null && user.isEmailVerified()) {
                        emailVerificationCheckbox.setVisibility(View.VISIBLE);
                        emailVerificationCheckbox.setChecked(true);
                        handler.removeCallbacks(verificationCheck);
                    } else {
                        handler.postDelayed(verificationCheck, 3000);
                    }
                });
            }
        };

        if (user != null && !user.isEmailVerified()) {
            handler.post(verificationCheck);
        }
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
            Intent intent = new Intent(NameActivity.this, InviteCodeActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("email", email);
            intent.putExtra("password", password);
            intent.putExtra("uri", resultUri.toString());

            Date myDate = new Date();
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String date = format.format(myDate);
            Random r = new Random();
            int n = 100000 + r.nextInt(900000);
            String code = String.valueOf(n);
            String isSharing = "false";

            intent.putExtra("code", code);
            intent.putExtra("isSharing", isSharing);

            startActivity(intent);
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
