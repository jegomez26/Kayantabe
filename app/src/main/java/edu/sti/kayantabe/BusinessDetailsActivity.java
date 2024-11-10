package edu.sti.kayantabe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BusinessDetailsActivity extends AppCompatActivity {

    private EditText etBusinessName, etProvince, etCity, etBusinessAddress;
    private Spinner spinnerBarangay;
    private CheckBox cbHouseCleaning, cbLaundry, cbPlumbing, cbGardening;
    private Button btnSubmit, btnUploadPermit;
    private ImageView imgPermitPlaceholder;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri businessPermitUri;

    // Firebase references
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseFirestore db;
    private String userId;
    private String permitBase64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        etBusinessName = findViewById(R.id.etBusinessName);
        etProvince = findViewById(R.id.etProvince);
        etCity = findViewById(R.id.etCity);
        etBusinessAddress = findViewById(R.id.etBusinessAddress);
        spinnerBarangay = findViewById(R.id.spinnerBarangay);
        cbHouseCleaning = findViewById(R.id.cbHouseCleaning);
        cbLaundry = findViewById(R.id.cbLaundry);
        cbPlumbing = findViewById(R.id.cbPlumbing);
        cbGardening = findViewById(R.id.cbGardening);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnUploadPermit = findViewById(R.id.btnUploadPermit);
        imgPermitPlaceholder = findViewById(R.id.imgPermitPlaceholder);

        // Firebase initialization
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get the current user ID

        // Set default values for Province and City
        etProvince.setText("Pampanga");
        etCity.setText("Angeles City");

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

        // Setup the button for uploading the business permit image
        btnUploadPermit.setOnClickListener(v -> uploadPermit());

        // Handle submit button click
        btnSubmit.setOnClickListener(v -> handleSubmit());
    }

    private void uploadPermit() {
        Intent intent = new Intent();
        intent.setType("image/*"); // Allow images only
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Business Permit Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            businessPermitUri = data.getData();
            imgPermitPlaceholder.setImageURI(businessPermitUri); // Display the selected image

            // Convert image to Base64
            try {
                InputStream inputStream = getContentResolver().openInputStream(businessPermitUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                permitBase64 = encodeToBase64(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to encode image", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void handleSubmit() {
        String businessName = etBusinessName.getText().toString().trim();
        String businessAddress = etBusinessAddress.getText().toString().trim();
        String selectedBarangay = spinnerBarangay.getSelectedItem().toString();

        // Collect services
        List<String> services = new ArrayList<>();
        if (cbHouseCleaning.isChecked()) services.add("House Cleaning");
        if (cbLaundry.isChecked()) services.add("Laundry");
        if (cbPlumbing.isChecked()) services.add("Plumbing");
        if (cbGardening.isChecked()) services.add("Gardening");

        if (businessName.isEmpty() || businessAddress.isEmpty() || services.isEmpty() || permitBase64 == null) {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show();
            return;
        }

        // Submit business details to Firestore
        BusinessDetails businessDetails = new BusinessDetails(businessName, selectedBarangay, businessAddress, services, permitBase64);
        db.collection("service_providers").document(userId)
                .set(businessDetails)
                .addOnSuccessListener(aVoid -> {
                    // Update the users collection to set isBusinessDetailsComplete to true
                    db.collection("users").document(userId)
                            .update("isBusinessDetailsComplete", true)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Business details submitted for approval", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(BusinessDetailsActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to update user status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to submit details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Helper method to convert Bitmap to Base64
    private String encodeToBase64(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}