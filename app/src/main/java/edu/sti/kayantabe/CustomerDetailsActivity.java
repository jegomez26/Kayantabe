package edu.sti.kayantabe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerDetailsActivity extends AppCompatActivity {

    private EditText edtProvince, edtCity, edtOtherAddress, edtContactNumber;
    private Spinner spinnerBarangay;
    private Button btnSaveDetails;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_details);

        // Initialize Firebase Firestore and Auth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        edtProvince = findViewById(R.id.edtProvince);
        edtCity = findViewById(R.id.edtCity);
        edtOtherAddress = findViewById(R.id.edtOtherAddress);
        edtContactNumber = findViewById(R.id.edtContactNumber);
        spinnerBarangay = findViewById(R.id.spinnerBarangay);
        btnSaveDetails = findViewById(R.id.btnSaveDetails);

        // Populate Barangay Spinner
        List<String> barangayList = new ArrayList<>();
        // Add barangay items as before
        barangayList.add("Agapito del Rosario");
        barangayList.add("Amsic");
        barangayList.add("Anunas");
        barangayList.add("Balibago");
        barangayList.add("Capaya");
        barangayList.add("Claro M. Recto");
        barangayList.add("Cuayan");
        barangayList.add("Cutcut");
        barangayList.add("Cutud");
        barangayList.add("Lourdes North West");
        barangayList.add("Lourdes Sur (Talimundoc)");
        barangayList.add("Lourdes Sur East");
        barangayList.add("Malabanias");
        barangayList.add("Margot");
        barangayList.add("Ninoy Aquino (Marisol)");
        barangayList.add("Mining");
        barangayList.add("Pampang");
        barangayList.add("Pandan");
        barangayList.add("Pulungbulu");
        barangayList.add("PulungCacutud");
        barangayList.add("PulungMaragul");
        barangayList.add("Salapungan");
        barangayList.add("San José");
        barangayList.add("San Isidro");
        barangayList.add("San Juan (Maglalambing)");
        barangayList.add("San Nicolas");
        barangayList.add("San Vicente");
        barangayList.add("Santo Niño");
        barangayList.add("Santo Rosario");
        barangayList.add("Santo Tomas");
        barangayList.add("Santiago");
        barangayList.add("Sapang Bato");

        ArrayAdapter<String> barangayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, barangayList);
        barangayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBarangay.setAdapter(barangayAdapter);

        // Save details on button click
        btnSaveDetails.setOnClickListener(v -> saveDetails());
    }

    private void saveDetails() {
        String otherAddress = edtOtherAddress.getText().toString().trim();
        String contactNumber = edtContactNumber.getText().toString().trim();
        String barangay = spinnerBarangay.getSelectedItem().toString();
        String province = edtProvince.getText().toString();
        String city = edtCity.getText().toString();

        // Validation
        if (otherAddress.isEmpty() || contactNumber.isEmpty()) {
            Toast.makeText(this, "Please fill in all details.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a Map for the customer data
        Map<String, Object> customerData = new HashMap<>();
        customerData.put("province", province);
        customerData.put("city", city);
        customerData.put("barangay", barangay);
        customerData.put("otherAddress", otherAddress);
        customerData.put("contactNumber", contactNumber);

        // Get the current user's ID (this will be used as the document ID)
        String userId = auth.getCurrentUser().getUid();

        // Save the data to Firestore under the "customers" collection
        db.collection("customers")
                .document(userId) // Use user ID as the document ID
                .set(customerData)
                .addOnSuccessListener(aVoid -> {
                    // Success, data saved
                    Toast.makeText(CustomerDetailsActivity.this, "Details saved successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CustomerDetailsActivity.this, CustomerDashboardActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Failure, show error message
                    Toast.makeText(CustomerDetailsActivity.this, "Error saving details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
