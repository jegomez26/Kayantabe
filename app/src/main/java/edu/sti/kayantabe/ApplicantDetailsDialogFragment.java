package edu.sti.kayantabe;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.FirebaseFirestore;

public class ApplicantDetailsDialogFragment extends DialogFragment {

    private String applicantId;
    private String permitBase64;
    private String businessName;
    private String businessAddress;
    private String barangay;
    private String servicesOffered;
    private String representativeName;

    public static ApplicantDetailsDialogFragment newInstance(String applicantId, String permitBase64,
                                                             String businessName, String businessAddress,
                                                             String barangay, String servicesOffered) {
        ApplicantDetailsDialogFragment fragment = new ApplicantDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putString("applicantId", applicantId);
        args.putString("permitBase64", permitBase64);
        args.putString("businessName", businessName);
        args.putString("businessAddress", businessAddress);
        args.putString("barangay", barangay);
        args.putString("servicesOffered", servicesOffered);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_applicant_details, null);

        if (getArguments() != null) {
            applicantId = getArguments().getString("applicantId");
            permitBase64 = getArguments().getString("permitBase64");
            businessName = getArguments().getString("businessName");
            businessAddress = getArguments().getString("businessAddress");
            barangay = getArguments().getString("barangay");
            servicesOffered = getArguments().getString("servicesOffered");
        }

        // Fetch the representative name and business details
        fetchRepresentativeName(dialogView);
        fetchBusinessDetails(dialogView);

        // Handle the 'View Business Permit' button
        Button btnViewPermit = dialogView.findViewById(R.id.btnViewPermit);
        btnViewPermit.setOnClickListener(v -> {
            showPermitDialog();
        });

        // Handle the 'Approve' button
        Button btnApprove = dialogView.findViewById(R.id.btnApprove);
        btnApprove.setOnClickListener(v -> {
            approveApplicant();
        });

        // Handle the 'Reject' button
        Button btnReject = dialogView.findViewById(R.id.btnReject);
        btnReject.setOnClickListener(v -> {
            rejectApplicant();
        });

        builder.setView(dialogView)
                .setTitle("Applicant Details")
                .setNegativeButton("Close", (dialog, id) -> dialog.dismiss());

        return builder.create();
    }

    private void fetchRepresentativeName(View dialogView) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(applicantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get representative name from the 'users' collection
                        representativeName = documentSnapshot.getString("name");

                        // Display the representative name in the dialog
                        TextView dialogRepresentativeName = dialogView.findViewById(R.id.dialogRepresentativeName);
                        dialogRepresentativeName.setText("Representative: " + representativeName);
                    } else {
                        Toast.makeText(getActivity(), "Representative not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ApplicantDetails", "Error fetching representative name: " + e.getMessage());
                    Toast.makeText(getActivity(), "Failed to fetch representative name", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchBusinessDetails(View dialogView) {
        TextView dialogBusinessName = dialogView.findViewById(R.id.dialogBusinessName);
        TextView dialogLocation = dialogView.findViewById(R.id.dialogLocation);
        TextView dialogServicesOffered = dialogView.findViewById(R.id.dialogServicesOffered);

        dialogBusinessName.setText("Business: " + businessName);
        dialogLocation.setText("Location: " + businessAddress + ", " + barangay);
        dialogServicesOffered.setText("Services Offered: " + servicesOffered);

        // Fetch the business permit URL from Firestore (field name: businessPermitUrl)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("service_providers").document(applicantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Ensure the field name is correctly matched with your Firestore structure
                        permitBase64 = documentSnapshot.getString("businessPermitUrl");

                        // If permitBase64 is null, handle the error (optional)
                        if (permitBase64 == null || permitBase64.isEmpty()) {
                            Toast.makeText(getActivity(), "No business permit found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Applicant not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ApplicantDetails", "Error fetching business permit: " + e.getMessage());
                    Toast.makeText(getActivity(), "Failed to fetch business permit", Toast.LENGTH_SHORT).show();
                });
    }

    private void showPermitDialog() {
        // Decode the base64 string to a Bitmap
        byte[] decodedBytes = Base64.decode(permitBase64, Base64.DEFAULT);
        Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

        // Create a dialog to show the permit
        AlertDialog.Builder permitDialogBuilder = new AlertDialog.Builder(getActivity());
        View permitDialogView = getLayoutInflater().inflate(R.layout.dialog_view_permit, null);
        ImageView permitImageView = permitDialogView.findViewById(R.id.imgPermit);
        permitImageView.setImageBitmap(decodedImage);

        permitDialogBuilder.setView(permitDialogView)
                .setTitle("Business Permit")
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void approveApplicant() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Update the status of the applicant in the service_providers collection
        db.collection("service_providers").document(applicantId)
                .update("status", "approved")
                .addOnSuccessListener(aVoid -> {
                    // After approving the applicant, update the approval status in the users collection
                    db.collection("users").document(applicantId)
                            .update("isApproved", true)  // Set isApproved to true for approval
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(getActivity(), "Applicant approved and status updated", Toast.LENGTH_SHORT).show();

                                dismiss();  // Close the dialog
                            })
                            .addOnFailureListener(e -> {
                                Log.e("ApplicantDetails", "Error updating user approval status: " + e.getMessage());
                                Toast.makeText(getActivity(), "Failed to update user approval status", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("ApplicantDetails", "Error approving applicant: " + e.getMessage());
                    Toast.makeText(getActivity(), "Failed to approve applicant", Toast.LENGTH_SHORT).show();
                });
    }


    private void rejectApplicant() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Update the status of the applicant in the service_providers collection
        db.collection("service_providers").document(applicantId)
                .update("status", "rejected")
                .addOnSuccessListener(aVoid -> {
                    // After rejecting the applicant, update the approval status in the users collection
                    db.collection("users").document(applicantId)
                            .update("isApproved", false)  // Set isApproved to false for rejection
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(getActivity(), "Applicant rejected and status updated", Toast.LENGTH_SHORT).show();

                                dismiss();  // Close the dialog
                            })
                            .addOnFailureListener(e -> {
                                Log.e("ApplicantDetails", "Error updating user approval status: " + e.getMessage());
                                Toast.makeText(getActivity(), "Failed to update user approval status", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("ApplicantDetails", "Error rejecting applicant: " + e.getMessage());
                    Toast.makeText(getActivity(), "Failed to reject applicant", Toast.LENGTH_SHORT).show();
                });
    }
}
