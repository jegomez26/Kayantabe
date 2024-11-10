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

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etPosition, etEmail, etPassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etName = findViewById(R.id.etName);
        etPosition = findViewById(R.id.etPosition);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnRegister.setOnClickListener(v -> createAccount());
    }

    private void createAccount() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String position = etPosition.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || position.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            sendEmailVerification(user);
                            String userId = user.getUid();
                            saveUserToFirestore(userId, name, position, email);
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Verification email sent. Please verify your email.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(RegisterActivity.this, EmailVerificationActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String userId, String name, String position, String email) {
        HashMap<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("position", position);
        user.put("email", email);
        user.put("role", "ServiceProvider");
        user.put("isApproved", false);

        db.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User info saved to Firestore"))
                .addOnFailureListener(e -> Log.d(TAG, "saveUserToFirestore: " + e.getMessage()));
    }
}