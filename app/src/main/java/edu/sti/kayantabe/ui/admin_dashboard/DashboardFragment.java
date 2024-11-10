package edu.sti.kayantabe.ui.admin_dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

import edu.sti.kayantabe.R;
import edu.sti.kayantabe.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private TextView applicantsCount, serviceProvidersCount, customersCount;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        applicantsCount = root.findViewById(R.id.applicantsCount);
        serviceProvidersCount = root.findViewById(R.id.serviceProvidersCount);
        customersCount = root.findViewById(R.id.customersCount);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch Applicants Count
        db.collection("users")
                .whereEqualTo("isBusinessDetailsComplete", true)  // Business details should be complete
                .whereIn("isApproved", Arrays.asList(false, null))  // isApproved should be false or null
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int applicantsCountValue = task.getResult().size(); // Get the count of applicants
                        applicantsCount.setText(String.valueOf(applicantsCountValue));  // Display the count
                    } else {
                        Log.e("ApplicantsCount", "Error fetching applicants count: " + task.getException().getMessage());
                    }
                });


        // Fetch Service Providers Count
        db.collection("users")
                .whereEqualTo("role", "ServiceProvider")   // Filter by role = "ServiceProvider"
                .whereEqualTo("isApproved", true)          // Filter where isApproved is true
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        serviceProvidersCount.setText(String.valueOf(task.getResult().size()));  // Display the count
                    } else {
                        Log.e("ServiceProvidersCount", "Error fetching service providers count: " + task.getException().getMessage());
                    }
                });


        // Fetch Customers Count
        db.collection("users").whereEqualTo("role", "customer")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        customersCount.setText(String.valueOf(task.getResult().size()));
                    }
                });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}