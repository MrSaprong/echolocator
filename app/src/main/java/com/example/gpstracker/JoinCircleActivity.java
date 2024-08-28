package com.example.gpstracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.goodiebag.pinview.Pinview;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class JoinCircleActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    private DatabaseReference reference, currentReference;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private String currentUserId, joinUserId;
    private DatabaseReference circleReference;
    private Pinview pinview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_circle);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        currentReference = reference.child(user.getUid());
        currentUserId = user.getUid();

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return position == 0 ? new PinViewFragment() : new ScanQRFragment();
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Enter Code" : "Scan QR");
        }).attach();
    }

    public void submitPinCode(String pinCode) {
        Query query = reference.orderByChild("code").equalTo(pinCode);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    handleJoinCircle(dataSnapshot);
                } else {
                    Toast.makeText(getApplicationContext(), "Circle code not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleJoinCircle(DataSnapshot dataSnapshot) {
        for (DataSnapshot childDss : dataSnapshot.getChildren()) {
            CreateUser createUser = childDss.getValue(CreateUser.class);
            joinUserId = createUser.userId;

            if (joinUserId.equals(currentUserId)) {
                Toast.makeText(getApplicationContext(), "You cannot join your own circle", Toast.LENGTH_SHORT).show();
                return;
            }

            String circleId = createUser.code;
            String circleName = createUser.name + " circle";

            circleReference = reference.child(joinUserId).child("CircleMembers");
            CircleJoin circleJoin = new CircleJoin(currentUserId);

            circleReference.child(user.getUid()).setValue(circleJoin).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "User joined circle successfully", Toast.LENGTH_SHORT).show();
                    joinCircle(circleId, circleName);
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to join circle", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void joinCircle(String circleId, String circleName) {
        DatabaseReference userCirclesRef = currentReference.child("joinedCircles");

        Map<String, Object> circleData = new HashMap<>();
        circleData.put("name", circleName);

        userCirclesRef.child(circleId).setValue(circleData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(JoinCircleActivity.this, "Joined circle successfully", Toast.LENGTH_SHORT).show();
                navigateToJoinedCircleActivity();
            } else {
                Toast.makeText(JoinCircleActivity.this, "Failed to join circle", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToJoinedCircleActivity() {
        Intent intent = new Intent(JoinCircleActivity.this, JoinedCircleActivity.class);
        startActivity(intent);
        finish();
    }

    public void submitButtonClick(View v) {
        String pinCode = pinview.getValue(); // Assuming pinview is initialized somewhere in the activity
        submitPinCode(pinCode);
    }

    public void handleScannedQRCode(String scannedCode) {
        submitPinCode(scannedCode);
    }

   /*@Override
    public void onBackPressed() {
        Intent intent = new Intent(JoinCircleActivity.this, UserLocationMainActivity.class);
        startActivity(intent);
        finish();
    }*/

}