package edu.sti.kayantabe;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegisterLink;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegisterLink = findViewById(R.id.btnRegisterLink);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> loginUser());

        btnRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationOptionsActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please enter all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Retrieve user role after successful login
                        String userId = mAuth.getCurrentUser().getUid();
                        Log.i(TAG, "loginUser: " + userId);
                        fetchUserRole(userId);
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "loginUser: " + task.getException());
                    }
                });
    }

    private void fetchUserRole(String userId) {
        // Assuming roles are stored in Firestore under a "users" collection
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String role = document.getString("role");
                            redirectToRoleActivity(role, document);
                        } else {
                            Toast.makeText(LoginActivity.this, "User role not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Failed to retrieve user role.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void redirectToRoleActivity(String role, DocumentSnapshot document) {
        Intent intent;
        FirebaseUser user = mAuth.getCurrentUser();

        switch (role) {
            case "Admin":
                intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
                break;
            case "ServiceProvider":
                Boolean isBusinessDetailsComplete = document.getBoolean("isBusinessDetailsComplete");
                Boolean isApproved = document.getBoolean("isApproved");
                if (isBusinessDetailsComplete != null && isBusinessDetailsComplete && Boolean.FALSE.equals(isApproved)) {
                    // Redirect to ServiceProviderDashboardActivity if business details are complete
                    Toast.makeText(this, "We're still waiting for the approval of admin.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (isApproved != null && isApproved) {
                    intent = new Intent(LoginActivity.this, ServiceProviderDashboardActivity.class);
                    startActivity(intent);
                } else {
                    // Redirect to BusinessDetailsActivity if business details are incomplete
                    intent = new Intent(LoginActivity.this, BusinessDetailsActivity.class);
                    startActivity(intent);
                }
                break;
            case "Customer":
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null && currentUser.isEmailVerified()) {
                    // Check if customer details are complete
                    Boolean isCustomerDetailsComplete = document.getBoolean("isCustomerDetailsComplete");
                    if (isCustomerDetailsComplete != null && isCustomerDetailsComplete) {
                        // If customer details are complete, go to CustomerDashboardActivity
                        intent = new Intent(LoginActivity.this, CustomerDashboardActivity.class);
                    } else {
                        // If customer details are not complete, go to CustomerDetailsActivity
                        intent = new Intent(LoginActivity.this, CustomerDetailsActivity.class);
                    }
                    startActivity(intent);
                } else {
                    // If customer email is not verified, show a toast
                    Toast.makeText(this, "Please verify your email to continue.", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                Toast.makeText(this, "Unknown role.", Toast.LENGTH_SHORT).show();
                return;
        }
        finish(); // Close the login activity once the user is redirected
    }
}
