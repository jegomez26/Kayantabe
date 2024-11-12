package edu.sti.kayantabe.ui.sp_services;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.FirebaseFirestore;

import edu.sti.kayantabe.R;
import edu.sti.kayantabe.Service;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class EditServiceDialogFragment extends DialogFragment {

    private static final String ARG_SERVICE = "service";
    private static final int PICK_IMAGE_REQUEST = 1;

    private Service service;
    private EditText nameEditText, descriptionEditText, priceEditText;
    private ImageView serviceImageView;
    private Button selectImageButton, saveButton;
    private FirebaseFirestore db;
    private Bitmap selectedImageBitmap;

    // Define the listener interface
    public interface OnServiceUpdatedListener {
        void onServiceUpdated();
    }

    private OnServiceUpdatedListener listener;

    public EditServiceDialogFragment() {
        // Required empty public constructor
    }

    public static EditServiceDialogFragment newInstance(Service service) {
        EditServiceDialogFragment fragment = new EditServiceDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SERVICE, (Serializable) service);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            service = (Service) getArguments().getSerializable(ARG_SERVICE);
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_service_dialog, container, false);

        nameEditText = view.findViewById(R.id.editTextServiceName);
        descriptionEditText = view.findViewById(R.id.editTextServiceDescription);
        priceEditText = view.findViewById(R.id.editTextServicePrice);
        serviceImageView = view.findViewById(R.id.imageViewServiceImage);
        selectImageButton = view.findViewById(R.id.btnSelectImage);
        saveButton = view.findViewById(R.id.btnSaveService);

        // Set initial values if available
        if (service != null) {
            nameEditText.setText(service.getName());
            descriptionEditText.setText(service.getDescription());
            priceEditText.setText(String.valueOf(service.getPrice()));
            // Load existing image if available (requires image loading library e.g., Glide or Picasso)
        }

        selectImageButton.setOnClickListener(v -> openImageSelector());
        saveButton.setOnClickListener(v -> saveServiceChanges());

        return view;
    }

    public void setOnServiceUpdatedListener(OnServiceUpdatedListener listener) {
        this.listener = listener;
    }

    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            try {
                // Get the selected image URI
                Uri imageUri = data.getData();
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                serviceImageView.setImageBitmap(selectedImageBitmap); // Preview the selected image
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveServiceChanges() {
        String name = nameEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String priceStr = priceEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }

        service.setName(name);
        service.setDescription(description);
        service.setPrice(price);

        if (selectedImageBitmap != null) {
            String base64Image = convertImageToBase64(selectedImageBitmap);
            service.setImageUrl(base64Image); // Store Base64 encoded image in service object
        }

        saveServiceToFirestore();
    }

    private String convertImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void saveServiceToFirestore() {
        db.collection("services").document(service.getId())
                .set(service)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Service updated successfully", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onServiceUpdated();  // Notify the listener (i.e., ServicesFragment)
                    }
                    dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update service", Toast.LENGTH_SHORT).show());
    }
}
