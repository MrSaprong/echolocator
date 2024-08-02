package com.example.gpstracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.gpstracker.databinding.ActivityMyNavigationBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

// Main activity for navigation using Navigation Component and Drawer Layout
public class MyNavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // Implementing NavigationView.OnNavigationItemSelectedListener
    private FirebaseAuth auth;

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMyNavigationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Using view binding to inflate the layout
        binding = ActivityMyNavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initializing FirebaseAuth instance
        auth = FirebaseAuth.getInstance();

        // Setting up the toolbar
        setSupportActionBar(binding.appBarMyNavigation.toolbar);

        // Setting up the FloatingActionButton (FAB) click listener
        binding.appBarMyNavigation.navHostFragmentContentMyNavigation.setOnClickListener(view -> {
            // Showing a Snackbar message when FAB is clicked
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });

        // Setting up the DrawerLayout and NavigationView
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Configuring the AppBarConfiguration with the IDs of the top-level destinations
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_signOut)
                .setOpenableLayout(drawer)
                .build();

        // Finding the NavController from the host fragment
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_my_navigation);

        // Setting up the ActionBar with the NavController and AppBarConfiguration
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        // Setting up the NavigationView with the NavController
        NavigationUI.setupWithNavController(navigationView, navController);

        // Setting the NavigationItemSelectedListener to this activity
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.my_navigation, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle navigation when the up button is pressed
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_my_navigation);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here
        int id = item.getItemId();
        if (id == R.id.nav_signOut) {
            // Handle the sign out action
            auth.signOut();
            Intent intent = new Intent(MyNavigationActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_inviteMembers) {
            // Handle invite members action
        } else if (id == R.id.nav_joinCircle) {
            // Handle join circle action
        } else if (id == R.id.nav_joinedCircles) {
            // Handle joined circles action
        } else if (id == R.id.nav_shareLoc) {
            // Handle share location action
        } else if (id == R.id.nav_myCircle) {
            // Handle my circle action
        }

        // Close the navigation drawer after selection
        DrawerLayout drawer = binding.drawerLayout;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
