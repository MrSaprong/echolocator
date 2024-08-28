package com.example.gpstracker;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JoinedCircleActivity extends AppCompatActivity {

    private LinearLayout circlesContainer;
    private DatabaseReference userCirclesRef;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joined_circle);

        circlesContainer = findViewById(R.id.circles_container);
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            userCirclesRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(user.getUid()).child("joinedCircles");

            // Load joined circles
            loadJoinedCircles();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadJoinedCircles() {
        userCirclesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                circlesContainer.removeAllViews();
                boolean hasCircles = false;

                for (DataSnapshot circleSnapshot : snapshot.getChildren()) {
                    String circleId = circleSnapshot.getKey();
                    String circleName = circleSnapshot.child("name").getValue(String.class);

                    Log.d("JoinedCircleActivity", "Circle ID: " + circleId + ", Circle Name: " + circleName);

                    if (circleId != null && circleName != null) {
                        hasCircles = true;
                        // Fetch the number of users in the circle
                        DatabaseReference circleUsersRef = FirebaseDatabase.getInstance().getReference("Circles").child(circleId).child("members");
                        circleUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                int memberCount = (int) dataSnapshot.getChildrenCount();
                                addCircleCard(circleId, circleName, memberCount);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(JoinedCircleActivity.this, "Failed to load member count", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                if (hasCircles) {
                    findViewById(R.id.no_circles_text).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.no_circles_text).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(JoinedCircleActivity.this, "Failed to load circles", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void addCircleCard(String circleId, String circleName, int memberCount) {
        CardView cardView = (CardView) getLayoutInflater().inflate(R.layout.item_circle_card, circlesContainer, false);

        TextView circleNameTextView = cardView.findViewById(R.id.circle_name);
        TextView memberCountTextView = cardView.findViewById(R.id.member_count); // Assume this TextView exists in your layout

        circleNameTextView.setText(circleName);
        memberCountTextView.setText("Members: " + memberCount);

        cardView.setOnLongClickListener(v -> {
            showPopupMenu(v, circleId);
            return true;
        });

        circlesContainer.addView(cardView);
    }


    private void showPopupMenu(View view, String circleId) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.circle_popup_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.leave_circle) {
                confirmLeaveCircle(circleId);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void confirmLeaveCircle(String circleId) {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to leave this circle?")
                .setPositiveButton("Yes", (dialog, which) -> leaveCircle(circleId))
                .setNegativeButton("No", null)
                .show();
    }

    private void leaveCircle(String circleId) {
        userCirclesRef.child(circleId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(JoinedCircleActivity.this, "Left the circle", Toast.LENGTH_SHORT).show();
                loadJoinedCircles(); // Refresh the list
            } else {
                Toast.makeText(JoinedCircleActivity.this, "Failed to leave the circle", Toast.LENGTH_SHORT).show();
            }
        });
    }
}