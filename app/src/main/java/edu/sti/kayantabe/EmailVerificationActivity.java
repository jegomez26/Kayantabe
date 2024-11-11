package edu.sti.kayantabe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EmailVerificationActivity extends AppCompatActivity {

    private Button btnCheckVerification;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        btnCheckVerification = findViewById(R.id.btnCheckVerification);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnCheckVerification.setOnClickListener(v -> checkEmailVerification());
    }

    private void checkEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    // Get the user ID (UID)
                    String userId = user.getUid();

                    // Get the user's document from Firestore
                    DocumentReference userDocRef = db.collection("users").document(userId);

                    userDocRef.get().addOnCompleteListener(userTask -> {
                        if (userTask.isSuccessful()) {
                            String role = userTask.getResult().getString("role");

                            if ("ServiceProvider".equals(role)) {
                                // If the user is a service provider, go to BusinessDetailsActivity
                                startActivity(new Intent(EmailVerificationActivity.this, BusinessDetailsActivity.class));
                            } else if ("Customer".equals(role)) {
                                // If the user is a customer, go to CustomerDetailsActivity
                                startActivity(new Intent(EmailVerificationActivity.this, CustomerDetailsActivity.class));
                            } else {
                                // If the role is unknown or not set, show a message
                                Toast.makeText(EmailVerificationActivity.this, "Role not set, please contact support.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle failure in retrieving user role
                            Toast.makeText(EmailVerificationActivity.this, "Error retrieving user data.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "Please verify your email to continue.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
