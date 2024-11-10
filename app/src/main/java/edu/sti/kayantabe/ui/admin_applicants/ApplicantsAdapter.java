// ApplicantsAdapter.java
package edu.sti.kayantabe.ui.admin_applicants;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import edu.sti.kayantabe.Applicant;
import edu.sti.kayantabe.ApplicantDetailsDialogFragment;
import edu.sti.kayantabe.R;

public class ApplicantsAdapter extends RecyclerView.Adapter<ApplicantsAdapter.ViewHolder> {

    private Context context;
    private List<Applicant> applicants;

    public ApplicantsAdapter(Context context, List<Applicant> applicants) {
        this.context = context;
        this.applicants = applicants;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_applicant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Applicant applicant = applicants.get(position);
        String servicesOffered = String.join(", ", applicant.getServices()); // Assuming services is a List

        // Display data in the holder
        holder.businessName.setText(applicant.getBusinessName());
        holder.location.setText(applicant.getBusinessAddress() + ", " + applicant.getBarangay());
        holder.servicesOffered.setText(servicesOffered);

        // Fetch the representative name using the document ID (applicantId)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(applicant.getApplicantId())  // Use document ID as applicantId
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String representativeName = documentSnapshot.getString("name");
                        holder.representativeName.setText(representativeName);
                    } else {
                        holder.representativeName.setText("Not available");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ApplicantsAdapter", "Error fetching representative name: " + e.getMessage());
                });

        // Set up the item click listener to open the details dialog
        holder.itemView.setOnClickListener(v -> {
            ApplicantDetailsDialogFragment dialog = ApplicantDetailsDialogFragment.newInstance(
                    applicant.getApplicantId(),  // Pass document ID as applicantId
                    applicant.getPermitBase64(),
                    applicant.getBusinessName(),
                    applicant.getBusinessAddress(),
                    applicant.getBarangay(),
                    servicesOffered
            );
            dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "ApplicantDetailsDialog");
        });
    }



    @Override
    public int getItemCount() {
        return applicants.size();
    }

    // ViewHolder to hold views for each item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView businessName, representativeName, location, servicesOffered;

        public ViewHolder(View itemView) {
            super(itemView);
            businessName = itemView.findViewById(R.id.businessName);
            representativeName = itemView.findViewById(R.id.representativeName);
            location = itemView.findViewById(R.id.location);
            servicesOffered = itemView.findViewById(R.id.servicesOffered);
        }
    }

    public void updateApplicantsList(List<Applicant> newApplicants) {
        this.applicants = newApplicants;
        notifyDataSetChanged();
    }
}
