package com.example.gpstracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.core.view.View;

public class InviteCodeActivity extends AppCompatActivity {

    String name, email, password, issharing, code, date;
    Uri imageUri;
    ProgressDialog progressDialog = new ProgressDialog(this);

    TextView t1;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_code);
        t1 = (findViewById(R.id.textView));
        auth = FirebaseAuth.getInstance();
        Intent myIntent = getIntent();

        reference = FirebaseDatabase.getInstance().getReference().child("Users");


        if (myIntent != null) {
            name = myIntent.getStringExtra("name");
            email = myIntent.getStringExtra("email");
            password = myIntent.getStringExtra("password");
            code = myIntent.getStringExtra("code");
            issharing = myIntent.getStringExtra("isSharing");
            imageUri = myIntent.getParcelableExtra("uri");
        }
        t1.setText(code);

    }

    public void registerUser (View v){
        progressDialog.setMessage("Please wait. Your account is being created");
        progressDialog.show();

        auth.createUserWithEmailAndPassword(email,password)
                .addOnCanceledListener(){
                    @Override
                            public void onComplete(@NonNull Task<AuthResult> task){



            }
        }

    }
}