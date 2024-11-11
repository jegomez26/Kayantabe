package edu.sti.kayantabe.ui.sp_services;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import edu.sti.kayantabe.R;
import edu.sti.kayantabe.Service;

public class AddServiceDialogFragment extends DialogFragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText editTextServiceName;
    private EditText editTextServiceDescription;
    private EditText editTextServicePrice;
    private ImageView imageViewServiceImage;
    private String imageBase64 = ""; // Store the Base64 encoded image string
    private FirebaseFirestore db;
    private OnServiceAddedListener listener;

    public interface OnServiceAddedListener {
        void onServiceAdded();
    }

    public void setOnServiceAddedListener(OnServiceAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_service, container, false);

        editTextServiceName = view.findViewById(R.id.editTextServiceName);
        editTextServiceDescription = view.findViewById(R.id.editTextServiceDescription);
        editTextServicePrice = view.findViewById(R.id.editTextServicePrice);
        imageViewServiceImage = view.findViewById(R.id.imageViewServiceImage); // Add an ImageView for displaying selected image
        Button btnSaveService = view.findViewById(R.id.btnSaveService);
        Button btnSelectImage = view.findViewById(R.id.btnSelectImage); // Button to select image

        db = FirebaseFirestore.getInstance();

        btnSelectImage.setOnClickListener(v -> openImageChooser());

        btnSaveService.setOnClickListener(v -> {
            String name = editTextServiceName.getText().toString().trim();
            String description = editTextServiceDescription.getText().toString().trim();
            String priceStr = editTextServicePrice.getText().toString().trim();

            if (!name.isEmpty() && !description.isEmpty() && !priceStr.isEmpty() && !imageBase64.isEmpty()) {
                double price = Double.parseDouble(priceStr);

                // Get the current user's ID
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String userId = user != null ? user.getUid() : null;

                if (userId != null) {
                    // Create a new service object with the userId and imageUrl (Base64 encoded string)
                    Service newService = new Service(name, description, price, userId, imageBase64);

                    // Save the new service to Firestore
                    db.collection("services")
                            .add(newService)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(getActivity(), "Service added", Toast.LENGTH_SHORT).show();
                                if (listener != null) {
                                    listener.onServiceAdded();  // Notify the fragment to refresh
                                }
                                dismiss();  // Close dialog
                            })
                            .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to add service", Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(getActivity(), "User not authenticated", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Service Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                imageViewServiceImage.setImageBitmap(bitmap); // Display the selected image
                imageBase64 = convertBitmapToBase64(bitmap); // Convert to Base64
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
