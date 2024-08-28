package com.example.gpstracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karan.churi.PermissionManager.PermissionManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private PermissionManager manager;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            // Check if the user exists in the database
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User exists in the database, redirect to UserLocationMainActivity
                        Intent myIntent = new Intent(MainActivity.this, UserLocationMainActivity.class);
                        startActivity(myIntent);
                        finish(); // Close MainActivity
                    } else {
                        // User does not exist in the database, stay in MainActivity
                        setContentView(R.layout.activity_main);
                        manager = new PermissionManager() {};
                        manager.checkAndRequestPermissions(MainActivity.this);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database error
                    Toast.makeText(MainActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // User is not signed in, stay in MainActivity
            setContentView(R.layout.activity_main);
            manager = new PermissionManager() {};
            manager.checkAndRequestPermissions(this);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        manager.checkResult(requestCode, permissions, grantResults);

        ArrayList<String> deniedPermissions = manager.getStatus().get(0).denied;

        if (deniedPermissions.isEmpty()) {
            // Permissions are granted
        } else {
            // Handle the case where permissions are denied
        }
    }

    public void goToLogin(View v) {
        Intent myIntent = new Intent(MainActivity.this, LogInActivity.class);
        startActivity(myIntent);
    }

    public void goToRegister(View v) {
        Intent myIntent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(myIntent);
    }
}
