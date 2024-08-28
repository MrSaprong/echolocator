package com.example.gpstracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.goodiebag.pinview.Pinview;
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

public class PinViewFragment extends Fragment {

    private Pinview pinview;
    private DatabaseReference reference;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private String current_user_id;
    private DatabaseReference circleReference;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pin_view, container, false);

        pinview = view.findViewById(R.id.pinview);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        current_user_id = user != null ? user.getUid() : "";

        // Set the OnClickListener here
        pinview.setOnClickListener(v -> pinview.requestFocus());

        view.findViewById(R.id.submit).setOnClickListener(this::submitButtonClick);

        return view;
    }

    public void submitButtonClick(View v) {
        String pinValue = pinview.getValue();
        if (pinValue != null && !pinValue.isEmpty()) {
            Query query = reference.orderByChild("code").equalTo(pinValue);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot childDss : dataSnapshot.getChildren()) {
                            CreateUser createUser = childDss.getValue(CreateUser.class);
                            if (createUser != null) {
                                String join_user_id = createUser.userId;

                                if (join_user_id.equals(current_user_id)) {
                                    Toast.makeText(getContext(), "You cannot join your own circle", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Get the Circle ID and Circle Name from the createUser object
                                String circleId = createUser.code; // Assuming the code is used as the Circle ID
                                String circleName = createUser.name + " circle"; // Assuming name + "circle" as Circle Name

                                // Add the current user to the circle's members
                                circleReference = FirebaseDatabase.getInstance().getReference()
                                        .child("Circles").child(circleId).child("members");

                                CircleJoin circleJoin = new CircleJoin(current_user_id);

                                circleReference.child(current_user_id).setValue(circleJoin)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getContext(), "User joined circle successfully", Toast.LENGTH_SHORT).show();
                                                // Save Circle Data and Redirect to JoinedCircleActivity
                                                joinCircle(circleId, circleName);
                                            } else {
                                                Toast.makeText(getContext(), "Failed to join circle", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "Circle code not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Please enter a code", Toast.LENGTH_SHORT).show();
        }
    }

    private void joinCircle(String circleId, String circleName) {
        DatabaseReference userCirclesRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(current_user_id).child("joinedCircles");

        Map<String, Object> circleData = new HashMap<>();
        circleData.put("name", circleName);

        userCirclesRef.child(circleId).setValue(circleData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Joined circle successfully", Toast.LENGTH_SHORT).show();
                // Redirect to Joined Circles page
                Intent intent = new Intent(getActivity(), JoinedCircleActivity.class);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            } else {
                Toast.makeText(getContext(), "Failed to join circle", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
