package edu.sti.kayantabe;

import android.os.Bundle;
import android.view.View;
import android.view.Menu;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import edu.sti.kayantabe.databinding.ActivityEmployeeDashboardBinding;
import edu.sti.kayantabe.ui.emp_assigned_bookings.CurrentAssignedBookingsFragment;
import edu.sti.kayantabe.ui.emp_booking_history.PastAssignedBookingsFragment;

public class EmployeeDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_current_bookings) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new CurrentAssignedBookingsFragment())
                        .commit();
            } else if (item.getItemId() == R.id.nav_past_bookings) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new PastAssignedBookingsFragment())
                        .commit();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });


        // Load default fragment
        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_current_bookings);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CurrentAssignedBookingsFragment())
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
