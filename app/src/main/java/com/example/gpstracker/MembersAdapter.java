package com.example.gpstracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MembersViewHolder> {

    private ArrayList<CreateUser> namelist;
    private Context context;

    // Constructor
    public MembersAdapter(ArrayList<CreateUser> namelist, Context context) {
        this.namelist = namelist;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return namelist.size();
    }

    @NonNull
    @Override
    public MembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new MembersViewHolder(view, context, namelist);
    }

    @Override
    public void onBindViewHolder(@NonNull MembersViewHolder holder, int position) {
        CreateUser currentUser = namelist.get(position);
        holder.nameTextView.setText(currentUser.name);
        Picasso.get().load(currentUser.imageUrl).placeholder(R.drawable.defaultprofile).into(holder.circleImageView);

        // Corrected conditional logic for location sharing
        if ("true".equals(currentUser.issharing)) {
            holder.statusIcon.setImageResource(R.drawable.green_online); // Green icon
        } else {
            holder.statusIcon.setImageResource(R.drawable.red_offline); // Red icon
        }

        // Set onClickListener to show the PopupMenu
        holder.itemView.setOnClickListener(v -> holder.showPopupMenu(v, currentUser));

        // Set onLongClickListener to share location
        holder.itemView.setOnLongClickListener(v -> {
            holder.shareLocationWithMember(currentUser);
            return true;
        });

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(currentUser.userId);

        userRef.child("issharing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String sharingStatus = dataSnapshot.getValue(String.class);
                if ("true".equals(sharingStatus)) {
                    holder.statusIcon.setImageResource(R.drawable.green_online); // Green icon
                } else {
                    holder.statusIcon.setImageResource(R.drawable.red_offline); // Red icon
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });

    }


    // ViewHolder class
    public class MembersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nameTextView;
        CircleImageView circleImageView;
        Context context;
        ArrayList<CreateUser> nameArrayList;
        ImageView statusIcon;

        // Constructor
        public MembersViewHolder(@NonNull View itemView, Context context, ArrayList<CreateUser> nameArrayList) {
            super(itemView);
            this.context = context;
            this.nameArrayList = nameArrayList;

            // Initialize UI components
            nameTextView = itemView.findViewById(R.id.item_title);
            circleImageView = itemView.findViewById(R.id.i11);
            statusIcon = itemView.findViewById(R.id.status_share);

            // Set click listener for the item view
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                CreateUser clickedUser = nameArrayList.get(position);

                // Create an intent to open ProfileActivity
                Intent intent = new Intent(context, ProfileActivity.class);
                // Pass user data to ProfileActivity
                intent.putExtra("USER_NAME", clickedUser.name);
                intent.putExtra("USER_EMAIL", clickedUser.email);
                intent.putExtra("USER_IMAGE", clickedUser.imageUrl);
                intent.putExtra("USER_SHARING", clickedUser.issharing); // Keep as string since ProfileActivity expects it as a string

                // Start the ProfileActivity
                context.startActivity(intent);
            }
        }

        public void showPopupMenu(View view, CreateUser currentUser) {
            // Create a PopupMenu
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.inflate(R.menu.member_options_menu); // Inflate your menu resource file

            // Handle menu item clicks
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.view_profile) {
                    // Handle "View Profile" action
                    Intent intent = new Intent(view.getContext(), ProfileActivity.class);
                    intent.putExtra("USER_NAME", currentUser.name);
                    intent.putExtra("USER_EMAIL", currentUser.email);
                    intent.putExtra("USER_IMAGE", currentUser.imageUrl);
                    intent.putExtra("USER_SHARING", currentUser.issharing);
                    view.getContext().startActivity(intent);
                    return true;
                } else if (itemId == R.id.remove_member) {
                    // Handle "Remove Member" action
                    showRemoveMemberDialog(view.getContext(), currentUser);
                    return true;
                }
                return false;
            });

            // Show the popup menu
            popupMenu.show();
        }

        private void showRemoveMemberDialog(Context context, CreateUser currentUser) {
            new AlertDialog.Builder(context)
                    .setTitle("Remove Member")
                    .setMessage("Are you sure you want to remove " + currentUser.name + " from your circle?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Get the current user's ID (assumed to be the admin or the one initiating the action)
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        // Reference to the "Circles" node in the database
                        DatabaseReference circleRef = FirebaseDatabase.getInstance().getReference()
                                .child("Circles")
                                .child("members")
                                .child(currentUser.userId);

                        // Remove the member from the circle
                        circleRef.removeValue().addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Member removed successfully", Toast.LENGTH_SHORT).show();
                            namelist.remove(currentUser);
                            notifyDataSetChanged();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to remove member", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Dismiss the dialog
                        dialog.dismiss();
                    })
                    .show();
        }


        private void shareLocationWithMember(CreateUser currentUser) {
            // Fetch the current user's location
            // Assume we have a method to get the current location as a LatLng object
            LatLng currentLocation = getCurrentLocation();

            if (currentLocation != null) {
                DatabaseReference circleRef = FirebaseDatabase.getInstance().getReference()
                        .child("Circles")
                        .child("circleId")  // Replace with the actual circle ID
                        .child("locations")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                LocationData locationData = new LocationData(currentLocation.latitude, currentLocation.longitude, System.currentTimeMillis());

                circleRef.setValue(locationData).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Location shared with " + currentUser.name, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to share location", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(context, "Unable to retrieve current location", Toast.LENGTH_SHORT).show();
            }
        }

        private LatLng getCurrentLocation() {
            // Dummy method to return a current location.
            // Replace this with actual logic to get the user's current location
            // This may involve using FusedLocationProviderClient or any other location service

            // Example return:
            return new LatLng(37.7749, -122.4194);  // San Francisco coordinates
        }
    }
}
