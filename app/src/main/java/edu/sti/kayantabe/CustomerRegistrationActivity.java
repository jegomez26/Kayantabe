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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class CustomerRegistrationActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Register button click listener
        btnRegister.setOnClickListener(v -> createAccount());
    }

    private void createAccount() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Check if fields are filled
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Create account with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            sendEmailVerification(user);
                            String userId = user.getUid();
                            saveUserToFirestore(userId, name, email);
                        }
                    } else {
                        Toast.makeText(CustomerRegistrationActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(CustomerRegistrationActivity.this, "Verification email sent. Please verify your email.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(CustomerRegistrationActivity.this, EmailVerificationActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(CustomerRegistrationActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String userId, String name, String email) {
        HashMap<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("role", "Customer");
        user.put("isVerified", false);  // assuming customers are not immediately verified

        db.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User info saved to Firestore"))
                .addOnFailureListener(e -> Log.d(TAG, "saveUserToFirestore: " + e.getMessage()));
    }
}
