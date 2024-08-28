package com.example.gpstracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserLocationMainActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest request;
    private LatLng latLng;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private DrawerLayout drawerLayout;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String current_user_name;
    private String current_user_email;
    private String current_user_imageUrl;
    private TextView t1_currentName, t2_currentEmail;
    private ImageView i1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_location_main);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Error: Map Fragment is null", Toast.LENGTH_SHORT).show();
        }

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Set up the drawer layout and toggle
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set up the navigation view
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        t1_currentName = header.findViewById(R.id.title_text);
        t2_currentEmail = header.findViewById(R.id.email_text);
        i1 = header.findViewById(R.id.circleImageView);

        // Ensure that the ImageView is not null
        if (i1 == null) {
            Log.e("UserLocationMainActivity", "ImageView is null. Check your layout.");
            Toast.makeText(this, "ImageView is not initialized. Please check your layout.", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String uid = user.getUid();
                if (snapshot.hasChild(uid)) {
                    current_user_name = snapshot.child(uid).child("name").getValue(String.class);
                    current_user_email = snapshot.child(uid).child("email").getValue(String.class);
                    current_user_imageUrl = snapshot.child(uid).child("imageUrl").getValue(String.class);

                    if (current_user_name != null) t1_currentName.setText(current_user_name);
                    if (current_user_email != null) t2_currentEmail.setText(current_user_email);
                    if (current_user_imageUrl != null && i1 != null) {
                        Picasso.get().load(current_user_imageUrl).into(i1);
                    } else {
                        Log.e("UserLocationMainActivity", "Image URL or ImageView is null.");
                    }
                } else {
                    Toast.makeText(UserLocationMainActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DatabaseError", "Error: " + error.getMessage());
            }
        });
        checkPermissions();

        handleIncomingLocationIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingLocationIntent(intent);
    }

    private void handleIncomingLocationIntent(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                String path = data.getPath();
                if (path != null && path.contains("/maps/")) {
                    List<String> segments = data.getPathSegments();
                    if (segments.size() >= 2) {
                        String[] latLng = segments.get(segments.size() - 1).split(",");
                        if (latLng.length == 2) {
                            try {
                                double latitude = Double.parseDouble(latLng[0]);
                                double longitude = Double.parseDouble(latLng[1]);
                                dropPinAtLocation(new LatLng(latitude, longitude));
                            } catch (NumberFormatException e) {
                                Log.e("LocationError", "Invalid location format.");
                            }
                        }
                    }
                }
            }
        }
    }

    private void dropPinAtLocation(LatLng location) {
        if (mMap != null) {
            mMap.clear(); // Clear previous markers
            MarkerOptions options = new MarkerOptions().position(location).title("Shared Location");
            mMap.addMarker(options);
            float zoomLevel = 18.0f; // Adjust the zoom level for the dropped pin
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel));
        } else {
            Toast.makeText(this, "Map is not ready yet", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        enableUserLocation();

        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        client.connect();
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_CODE);
        } else {
            enableUserLocation();
        }
    }

    private void enableUserLocation() {
        if (mMap != null && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            fusedLocationClient.getLastLocation().addOnCompleteListener(this, task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    //mMap.addMarker(new MarkerOptions().position(userLocation).title("You are here"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                } else {
                    Toast.makeText(UserLocationMainActivity.this, "Unable to get last location", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (mMap == null) {
            Toast.makeText(this, "Map is not ready yet", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
            } else {
                Toast.makeText(this, "Permissions denied. Please grant permissions in settings.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_signOut) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else if (item.getItemId() == R.id.nav_myCircle) {
            Intent intent = new Intent(UserLocationMainActivity.this, MyCircleActivity.class);
            startActivity(intent);

        } else if (item.getItemId() == R.id.nav_joinCircle) {
            Intent myIntent = new Intent(UserLocationMainActivity.this, JoinCircleActivity.class);
            startActivity(myIntent);

        } else if (item.getItemId() == R.id.nav_joinedCircles) {
            Intent myIntent = new Intent(UserLocationMainActivity.this, JoinedCircleActivity.class);
            startActivity(myIntent);

        } else if (item.getItemId() == R.id.nav_inviteMembers) {
            // Handle invite members navigation
            Intent myIntent = new Intent(UserLocationMainActivity.this, InviteMemberActivity.class);
            startActivity(myIntent);

        } else if (item.getItemId() == R.id.nav_shareLoc) {
            if (latLng != null) {
                String uri = "http://maps.google.com/maps?q=loc:" + latLng.latitude + "," + latLng.longitude + " (" + current_user_name + "'s Location)";
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, "My location is: " + uri);
                startActivity(Intent.createChooser(i, "Share using: "));
            } else {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }



    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        request = LocationRequest.create();
        request.setInterval(3000);
        request.setFastestInterval(1000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection to Google API failed", Toast.LENGTH_SHORT).show();
    }

    private Marker currentLocationMarker;
    private boolean isMapZoomed = false;

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location == null) {
            Toast.makeText(getApplicationContext(), "Could not get Location", Toast.LENGTH_SHORT).show();
            return;
        }

        latLng = new LatLng(location.getLatitude(), location.getLongitude());

        // Update marker position or create it if it doesn't exist
        if (currentLocationMarker != null) {
            currentLocationMarker.setPosition(latLng);
        } else {
            MarkerOptions options = new MarkerOptions().position(latLng).title("You are here");
            currentLocationMarker = mMap.addMarker(options);
        }

        // Only zoom the map the first time location is updated
        if (!isMapZoomed) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20f));
            isMapZoomed = true;  // Set the flag to prevent further zooming
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }




    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection to Google API suspended", Toast.LENGTH_SHORT).show();
    }

}
