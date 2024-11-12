package edu.sti.kayantabe;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import edu.sti.kayantabe.databinding.ActivityServiceProviderDashboardBinding;
import edu.sti.kayantabe.EditBusinessDetailsFragment;

public class ServiceProviderDashboardActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityServiceProviderDashboardBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ImageView navLogoImageView;
    private String userId;
    private String newLogoBase64;

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
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_service_provider_dashboard);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_employees, R.id.nav_services)
                .setOpenableLayout(binding.drawerLayout)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Get the current user ID
        userId = mAuth.getCurrentUser().getUid();

        // Initialize the navigation header views
        navLogoImageView = binding.navView.getHeaderView(0).findViewById(R.id.imageView);

        // Fetch and display business details in the nav header
        updateNavHeader();
    }

    private void updateNavHeader() {
        // Fetch representative's name from 'users' collection
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String repName = documentSnapshot.getString("name");
                    TextView tvRepName = binding.navView.getHeaderView(0).findViewById(R.id.header_repName);
                    tvRepName.setText(repName);
                });

        // Fetch business name and logo from 'service_providers' collection
        db.collection("service_providers").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String businessName = documentSnapshot.getString("businessName");
                        String address = documentSnapshot.getString("businessAddress");
                        String barangay = documentSnapshot.getString("barangay");
                        String logoBase64 = documentSnapshot.getString("logoUrl");

                        TextView tvBusinessName = binding.navView.getHeaderView(0).findViewById(R.id.header_businessName);
                        tvBusinessName.setText(businessName);

                        // Fetch and decode the logo from Base64
                        if (logoBase64 != null && !logoBase64.isEmpty()) {
                            Bitmap decodedLogo = decodeBase64ToBitmap(logoBase64);
                            if (decodedLogo != null) {
                                navLogoImageView.setImageBitmap(decodedLogo);
                            }
                        } else {
                            navLogoImageView.setImageResource(R.drawable.ic_image); // Default image if logo not available
                        }

                        // Pass the business details to the fragment
                        navLogoImageView.setOnClickListener(v -> showEditBusinessDetailsFragment(businessName, address, barangay, logoBase64));
                    }
                });
    }

    private void showEditBusinessDetailsFragment(String businessName, String address, String barangay, String logoBase64) {
        // Create a new instance of EditBusinessDetailsFragment
        EditBusinessDetailsFragment fragment = new EditBusinessDetailsFragment(businessName, address, barangay, logoBase64);
        fragment.setOnBusinessDetailsListener(this::fetchBusinessDetails);
        fragment.show(getSupportFragmentManager(), "EditBusinessDetailsFragment");
    }

    private Bitmap decodeBase64ToBitmap(String base64String) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.service_provider_dashboard, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_service_provider_dashboard);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void fetchBusinessDetails() {
        db.collection("service_providers").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String businessName = documentSnapshot.getString("businessName");
                        String address = documentSnapshot.getString("businessAddress");
                        String barangay = documentSnapshot.getString("barangay");
                        String logoBase64 = documentSnapshot.getString("logoUrl");

                        // Update the business details in the navigation header
                        TextView tvBusinessName = binding.navView.getHeaderView(0).findViewById(R.id.header_businessName);
                        tvBusinessName.setText(businessName);

                        // Decode and display the logo if available
                        if (logoBase64 != null && !logoBase64.isEmpty()) {
                            Bitmap decodedLogo = decodeBase64ToBitmap(logoBase64);
                            if (decodedLogo != null) {
                                navLogoImageView.setImageBitmap(decodedLogo);
                            }
                        } else {
                            navLogoImageView.setImageResource(R.drawable.ic_image); // Default image if logo is unavailable
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch business details", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            showLogoutConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> performLogout())
                .setNegativeButton("No", null)
                .show();
    }

    private void performLogout() {
        // Logout from Firebase
        mAuth.signOut();

        // Clear the current activity stack and redirect to LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
