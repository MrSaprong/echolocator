package com.example.gpstracker;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InviteMemberActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_member);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase db = FirebaseDatabase.getInstance();

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = db.getReference("Users").child(userId);

        userRef.child("inviteCode").get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                String userCode = dataSnapshot.getValue(String.class);
                setupViewPager(userCode);  // Pass the code to the setupViewPager method
            } else {
                // Handle case where inviteCode does not exist
                Log.d("InviteMemberActivity", "Invite code does not exist for the user.");
            }
        }).addOnFailureListener(e -> {
            // Handle errors
            Log.e("InviteMemberActivity", "Failed to get invite code", e);
        });
    }

    private void setupViewPager(String userCode) {
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) {
                    return new InviteCodeFragment(userCode);  // Pass the code to the fragment
                } else {
                    return new QRCodeFragment(userCode);
                }
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Code");
            } else {
                tab.setText("QR Code");
            }
        }).attach();
    }
}
