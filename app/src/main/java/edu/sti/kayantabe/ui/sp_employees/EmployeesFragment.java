package edu.sti.kayantabe.ui.sp_employees;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import edu.sti.kayantabe.Employee;
import edu.sti.kayantabe.R;

public class EmployeesFragment extends Fragment {

    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int REQUEST_CODE_SMS_PERMISSION = 200;

    private RecyclerView recyclerView;
    private EmployeesAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String serviceProviderId;
    private String businessName;

    private List<Employee> employeeList = new ArrayList<>();
    private String employeeImageBase64 = "";
    private ImageView ivEmployeeProfilePicture;
    private String latestContactNumber;
    private String latestEmail;
    private String latestPassword;

    public EmployeesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employees, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewEmployees);
        FloatingActionButton fabAddEmployee = view.findViewById(R.id.fabAddEmployee);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        serviceProviderId = mAuth.getCurrentUser().getUid();
        db.collection("service_providers").document(serviceProviderId)
                .get().addOnSuccessListener(documentSnapshot -> {
                    businessName = documentSnapshot.getString("businessName");
                });

        // Initialize adapter
        adapter = new EmployeesAdapter(employeeList);
        recyclerView.setAdapter(adapter);

        // Load employees from Firestore
        loadEmployees();

        // Add employee dialog on FAB click
        fabAddEmployee.setOnClickListener(v -> showAddEmployeeDialog());

        return view;
    }

    private void loadEmployees() {
        db.collection("employees")
                .whereEqualTo("serviceProviderId", serviceProviderId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    employeeList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Employee employee = document.toObject(Employee.class);
                        employeeList.add(employee);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load employees", Toast.LENGTH_SHORT).show());
    }

    private void showAddEmployeeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add New Employee");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_employee, null);
        builder.setView(dialogView);

        EditText etFirstName = dialogView.findViewById(R.id.etEmployeeFirstName);
        EditText etLastName = dialogView.findViewById(R.id.etEmployeeLastName);
        EditText etContactNumber = dialogView.findViewById(R.id.etEmployeeContactNumber);
        ImageView ivEmployeeImage = dialogView.findViewById(R.id.ivEmployeeProfilePicture);
        Button btnChooseImage = dialogView.findViewById(R.id.btnChooseImage);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        ivEmployeeProfilePicture = ivEmployeeImage;

        // Image selection for profile picture
        btnChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        });

        // Convert selected image to base64 when onActivityResult is called
        ivEmployeeImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        });

        // Create and show the dialog, then use it to dismiss on cancel
        AlertDialog dialog = builder.create();

        // Save button click listener
        btnSave.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String contactNumber = etContactNumber.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || contactNumber.isEmpty() || employeeImageBase64.isEmpty()) {
                Toast.makeText(getContext(), "All fields and profile image are required", Toast.LENGTH_SHORT).show();
            } else {
                addEmployeeToFirestore(firstName, lastName, contactNumber, employeeImageBase64);
                dialog.dismiss();
            }
        });

        // Cancel button click listener
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap =MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                Bitmap rotatedImage = handleImageRotation(requireContext(), selectedImageUri);
                // Convert Bitmap to Base64
                String base64Image = bitmapToBase64(rotatedImage);
                // Update ImageView with the selected image
                ivEmployeeProfilePicture.setImageBitmap(rotatedImage);
                // Store the Base64 image string for later use
                employeeImageBase64 = base64Image;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        // Step 1: Resize the bitmap if it's too large
        int maxWidth = 800; // You can adjust this value as needed
        int maxHeight = 800; // You can adjust this value as needed
        bitmap = resizeBitmap(bitmap, maxWidth, maxHeight);

        // Step 2: Compress the bitmap to reduce quality
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream); // 60% quality
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // Step 3: Convert the compressed bitmap to Base64
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float aspectRatio = (float) width / height;
        if (width > maxWidth || height > maxHeight) {
            if (width > height) {
                width = maxWidth;
                height = Math.round(width / aspectRatio);
            } else {
                height = maxHeight;
                width = Math.round(height * aspectRatio);
            }
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private void addEmployeeToFirestore(String firstName, String lastName, String contactNumber, String imageBase64) {
        String employeeId = UUID.randomUUID().toString();
        String email = generateEmployeeEmail(firstName.charAt(0) + " " + lastName);
        String defaultPassword =
                firstName.substring(0, 1).toLowerCase() +
                        lastName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase() + "." +
                        contactNumber.substring(contactNumber.length() - 4);
        Employee employee = new Employee(employeeId, firstName , lastName, contactNumber, email, imageBase64, serviceProviderId);

        db.collection("employees").document(employeeId)
                .set(employee)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Employee added successfully", Toast.LENGTH_SHORT).show();
                    employeeList.add(employee);
                    adapter.notifyItemInserted(employeeList.size() - 1);

                    // Store values to retry SMS if permission is granted later
                    latestContactNumber = contactNumber;
                    latestEmail = email;
                    latestPassword = defaultPassword;

                    // Attempt to send SMS
//                    sendSmsNotification(contactNumber, email, defaultPassword);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add employee", Toast.LENGTH_SHORT).show());

        createEmployeeAuthAccount(email, defaultPassword, employee.getFirstName() + " " + employee.getLastName());
    }

    private String generateEmployeeEmail(String fullName) {
        String businessName = this.businessName; // Fetch the business name of the current user
        return fullName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase() +
                "@" +
                businessName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase() +
                ".com";
    }

    private void createEmployeeAuthAccount(String email, String defaultPassword, String name) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, defaultPassword)
                .addOnSuccessListener(authResult -> {
                    // Retrieve the UID of the newly created user
                    String uid = authResult.getUser().getUid();

                    // Create a map to store employee data
                    HashMap<String, Object> employeeData = new HashMap<>();
                    employeeData.put("email", email);
                    employeeData.put("isApproved", true);
                    employeeData.put("name", name);
                    employeeData.put("role", "Employee");

                    // Add employee data to the Firestore 'users' collection with the UID as the document ID
                    FirebaseFirestore.getInstance().collection("users").document(uid)
                            .set(employeeData)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(getContext(), "Employee account created and added to Firestore", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Failed to add employee to Firestore", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to create account", Toast.LENGTH_SHORT).show());
    }


    private void sendSmsNotification(String contactNumber, String email, String password) {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.SEND_SMS}, REQUEST_CODE_SMS_PERMISSION);
        } else {
            try {
                String message = "Welcome to KAYANTABE! Your account with " + businessName + " has been successfully created.\n\nLogin details:\nEmail: " + email + "\nPassword: " + password + "\n\nThank you for joining us. We’re excited to have you on board!";
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(contactNumber, null, message, null, null);
                Toast.makeText(getContext(), "SMS sent to employee", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.d("SMS", "Error sending SMS: " + e.getMessage());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Retry sending SMS after permission is granted
                sendSmsNotification(latestContactNumber, latestEmail, latestPassword); // Ensure you store these values before calling sendSmsNotification
            } else {
                Toast.makeText(getContext(), "SMS permission denied. Unable to send SMS notification.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public Bitmap handleImageRotation(Context context, Uri imageUri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);

        // Retrieve the image's orientation
        ExifInterface exif = new ExifInterface(context.getContentResolver().openInputStream(imageUri));
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        // Rotate the image based on its orientation
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(bitmap, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(bitmap, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(bitmap, 270);
            default:
                return bitmap;
        }
    }

    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

}
