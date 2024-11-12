package edu.sti.kayantabe;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import edu.sti.kayantabe.R;

public class EditBusinessDetailsFragment extends DialogFragment {

    private FirebaseFirestore db;
    private String userId;
    private EditText businessNameEditText, addressEditText;
    private ImageView logoImageView;
    private Spinner barangaySpinner;
    private String logoBase64;

    private String businessName;
    private String address;
    private String barangay;
    private static final int PICK_IMAGE_REQUEST = 1;
    private OnBusinessDetailsListener onBusinessDetailsListener;
    // Listener for activity to handle updates
    // Define an interface for the listener
    public interface OnBusinessDetailsListener {
        void onBusinessDetailsUpdated();
    }

    // Method to set the listener from Activity
    public void setOnBusinessDetailsListener(OnBusinessDetailsListener listener) {
        this.onBusinessDetailsListener = listener;
    }

    public EditBusinessDetailsFragment(String businessName, String address, String barangay, String logoBase64) {
        this.businessName = businessName;
        this.address = address;
        this.barangay = barangay;
        this.logoBase64 = logoBase64;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_business_details, container, false);

        // Set up views
        businessNameEditText = view.findViewById(R.id.etBusinessName);
        addressEditText = view.findViewById(R.id.etBusinessAddress);
        logoImageView = view.findViewById(R.id.imgLogoPlaceholder);
        barangaySpinner = view.findViewById(R.id.spinnerBarangay);

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

        ArrayAdapter<String> barangayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, barangayList);
        barangayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        barangaySpinner.setAdapter(barangayAdapter);

        // Pre-select the barangay value
        if (barangay != null && !barangay.isEmpty()) {
            int barangayPosition = barangayAdapter.getPosition(barangay);
            if (barangayPosition >= 0) {
                barangaySpinner.setSelection(barangayPosition);
            }
        }

        // Pre-fill fields with current data
        businessNameEditText.setText(businessName);
        addressEditText.setText(address);


        // Display the logo if available
        if (logoBase64 != null && !logoBase64.isEmpty()) {
            logoImageView.setImageBitmap(decodeBase64ToBitmap(logoBase64));
        } else {
            logoImageView.setImageResource(R.drawable.ic_image); // Default image
        }

        // Set up the upload logo button
        Button uploadLogoButton = view.findViewById(R.id.btnUploadLogo);
        uploadLogoButton.setOnClickListener(v -> openImagePicker());

        // Handle the Save button
        Button saveButton = view.findViewById(R.id.btnSubmit);
        saveButton.setOnClickListener(v -> saveBusinessDetails());

        return view;
    }

    // Open the image picker
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Handle image selection result
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                InputStream inputStream = getContext().getContentResolver().openInputStream(selectedImageUri);
                Bitmap selectedImageBitmap = BitmapFactory.decodeStream(inputStream);
                logoImageView.setImageBitmap(selectedImageBitmap);
                logoBase64 = encodeBitmapToBase64(selectedImageBitmap);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Encode Bitmap to Base64
    private String encodeBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // Decode Base64 to Bitmap
    private Bitmap decodeBase64ToBitmap(String base64) {
        byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    // Save business details
    private void saveBusinessDetails() {
        String newBusinessName = businessNameEditText.getText().toString().trim();
        String newAddress = addressEditText.getText().toString().trim();
        String newBarangay = barangaySpinner.getSelectedItem().toString();

        if (newBusinessName.isEmpty() || newAddress.isEmpty() || newBarangay.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update Firestore
        db.collection("service_providers").document(userId)
                .update("businessName", newBusinessName, "businessAddress", newAddress, "barangay", newBarangay, "logoUrl", logoBase64)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Business details updated", Toast.LENGTH_SHORT).show();
                        if (onBusinessDetailsListener != null) {
                            onBusinessDetailsListener.onBusinessDetailsUpdated();
                        }
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), "Failed to update details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Set dialog properties
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onBusinessDetailsListener != null) {
            onBusinessDetailsListener.onBusinessDetailsUpdated();
        }
    }
}
