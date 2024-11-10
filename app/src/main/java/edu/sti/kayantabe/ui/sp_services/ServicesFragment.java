package edu.sti.kayantabe.ui.sp_services;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import edu.sti.kayantabe.R;
import edu.sti.kayantabe.Service;
import edu.sti.kayantabe.databinding.FragmentServicesBinding;

public class ServicesFragment extends Fragment {

    private FragmentServicesBinding binding;
    private RecyclerView recyclerView;
    private ServicesAdapter servicesAdapter;
    private ArrayList<Service> servicesList;
    private FirebaseFirestore db;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_services, container, false);

        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView for displaying services
        recyclerView = view.findViewById(R.id.recyclerViewServices);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize services list and adapter
        servicesList = new ArrayList<>();
        servicesAdapter = new ServicesAdapter(servicesList);
        recyclerView.setAdapter(servicesAdapter);

        // Fetch services data from Firestore
        fetchServices();

        // Set up the FAB to add new service
        FloatingActionButton fabAddService = view.findViewById(R.id.fabAddService);
        fabAddService.setOnClickListener(v -> {
            // Show dialog to add a new service
            AddServiceDialogFragment newServiceDialog = new AddServiceDialogFragment();
            newServiceDialog.setOnServiceAddedListener(this::fetchServices);  // To refresh the service list after adding a new one
            newServiceDialog.show(getChildFragmentManager(), "AddServiceDialog");
        });

        return view;
    }

    private void fetchServices() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Fetch services for the current user only
            db.collection("services")
                    .whereEqualTo("userId", userId)  // Filter by userId
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        servicesList.clear();  // Clear existing services
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Service service = document.toObject(Service.class);
                            servicesList.add(service);
                        }
                        servicesAdapter.notifyDataSetChanged();  // Update the RecyclerView
                    })
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to load services", Toast.LENGTH_SHORT).show());
        }
    }

}
