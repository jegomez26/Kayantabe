package edu.sti.kayantabe;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import edu.sti.kayantabe.databinding.ActivityServiceProviderDashboardBinding;

public class ServiceProviderDashboardActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityServiceProviderDashboardBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView navRepNameTextView, navBusinessNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityServiceProviderDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();



        // Setup toolbar and drawer layout
        setSupportActionBar(binding.appBarServiceProviderDashboard.toolbar);


        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        View headerView = navigationView.getHeaderView(0);

        // Initialize the navigation header views
        navRepNameTextView = headerView.findViewById(R.id.header_repName);  // TextView for representative name
        navBusinessNameTextView = headerView.findViewById(R.id.header_businessName);  // TextView for business name

        // Setup navigation and drawer configuration
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_employees, R.id.nav_services)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_service_provider_dashboard);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Fetch data and update navigation header
        updateNavHeader();
    }

    private void updateNavHeader() {
        String userId = mAuth.getCurrentUser().getUid();

        // Fetch representative's name from 'users' collection
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String repName = documentSnapshot.getString("name");
                        navRepNameTextView.setText(repName);  // Set representative's name in navigation header
                    } else {
                        navRepNameTextView.setText("No name available");
                    }
                })
                .addOnFailureListener(e -> {
                    navRepNameTextView.setText("Error loading name");
                });

        // Fetch business name from 'service_providers' collection
        db.collection("service_providers")
                .document(userId)  // Ensure the service provider matches the current user
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.exists()) {
                        String businessName = queryDocumentSnapshots.getString("businessName");
                        navBusinessNameTextView.setText(businessName);  // Set business name in navigation header
                    } else {
                        navBusinessNameTextView.setText("No business name available");
                    }
                })
                .addOnFailureListener(e -> {
                    navBusinessNameTextView.setText("Error loading business name");
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if present
        getMenuInflater().inflate(R.menu.service_provider_dashboard, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_service_provider_dashboard);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
