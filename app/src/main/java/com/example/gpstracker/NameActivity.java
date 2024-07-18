package com.example.gpstracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressLint("Typos")
public class NameActivity extends AppCompatActivity {

    String email, password;
    EditText e5_name;
    CircleImageView circleImageView;
    Uri resultUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        e5_name = findViewById(R.id.editTextText);
        circleImageView = findViewById(R.id.circleImageView);

        Intent myIntent = getIntent();
        if (myIntent != null) {
            email = myIntent.getStringExtra("email");
            password = myIntent.getStringExtra("password");
        }
    }

    public void generateCode(View v) {
        Date myDate = new Date();
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.getDefault());
        String date = format1.format(myDate);

        Random r = new Random();
        int n = 100000 + r.nextInt(900000);
        String code = String.valueOf(n);

        if (resultUri != null) {
            Intent myIntent = new Intent(NameActivity.this, InviteCodeActivity.class);
            myIntent.putExtra("name", e5_name.getText().toString());
            myIntent.putExtra("email", email);
            myIntent.putExtra("password", password);
            myIntent.putExtra("date", date);
            myIntent.putExtra("isSharing", "false");
            myIntent.putExtra("code", code);
            myIntent.putExtra("imageUri", resultUri.toString());

            startActivity(myIntent);
        } else {
            Toast.makeText(getApplicationContext(), "Please choose an image", Toast.LENGTH_SHORT).show();
        }
    }

    public void selectImage(View v) {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(i, 12);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 12 && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            startCrop(selectedImageUri);
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                circleImageView.setImageURI(resultUri);
            }
        } else if (requestCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                Toast.makeText(this, cropError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCrop(@Nullable Uri uri) {
        String destinationFileName = "CroppedImage.jpg";
        if (uri != null) {
            Uri destinationUri = Uri.fromFile(new File(getCacheDir(), destinationFileName));
            UCrop.of(uri, destinationUri)
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(1080, 1080)
                    .start(this);
        }
    }
}
