package com.example.gpstracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyCircleActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    TextView emptyView; // Reference to the empty view TextView

    FirebaseAuth auth;
    FirebaseUser user;
    ArrayList<CreateUser> namelist = new ArrayList<>();
    DatabaseReference reference, usersReference;
    String circlememberid;

    private ValueEventListener circleMembersListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_circle);

        recyclerView = findViewById(R.id.recyclerview);
        emptyView = findViewById(R.id.empty_view); // Initialize empty view

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("CircleMembers");

        adapter = new MembersAdapter(namelist, MyCircleActivity.this);
        recyclerView.setAdapter(adapter);

        circleMembersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                namelist.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot dss : snapshot.getChildren()) {
                        circlememberid = dss.child("circlememberid").getValue(String.class);

                        if (circlememberid == null || circlememberid.isEmpty()) {
                            continue;
                        }

                        usersReference.child(circlememberid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        CreateUser createUser = snapshot.getValue(CreateUser.class);
                                        if (createUser != null) {
                                            namelist.add(createUser);
                                            adapter.notifyDataSetChanged();
                                        }

                                        // Toggle visibility based on the data
                                        toggleEmptyView();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    // Toggle visibility if snapshot doesn't exist
                    toggleEmptyView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        reference.addValueEventListener(circleMembersListener);
    }

    private void toggleEmptyView() {
        if (namelist.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (circleMembersListener != null) {
            reference.removeEventListener(circleMembersListener);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MyCircleActivity.this, UserLocationMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
