package edu.sti.kayantabe.ui.sp_services;

import android.app.AlertDialog;
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
import edu.sti.kayantabe.R;
import edu.sti.kayantabe.Service;
import edu.sti.kayantabe.databinding.FragmentServicesBinding;

import java.util.ArrayList;

public class ServicesFragment extends Fragment implements ServicesAdapter.OnServiceActionListener {

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
        servicesAdapter = new ServicesAdapter(servicesList, this);
        recyclerView.setAdapter(servicesAdapter);

        // Fetch services data from Firestore
        fetchServices();

        // Set up the FAB to add new service
        FloatingActionButton fabAddService = view.findViewById(R.id.fabAddService);
        fabAddService.setOnClickListener(v -> {
            AddServiceDialogFragment newServiceDialog = new AddServiceDialogFragment();
            newServiceDialog.setOnServiceAddedListener(this::fetchServices);
            newServiceDialog.show(getChildFragmentManager(), "AddServiceDialog");
        });

        return view;
    }

    private void fetchServices() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            db.collection("services")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        servicesList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Service service = document.toObject(Service.class);
                            service.setId(document.getId());
                            servicesList.add(service);
                        }
                        servicesAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to load services", Toast.LENGTH_SHORT).show());
        }
    }

    private void confirmDeleteService(Service service) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Service")
                .setMessage("Are you sure you want to delete this service?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.collection("services").document(service.getId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                fetchServices();
                                Toast.makeText(getContext(), "Service deleted successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete service", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void showEditServiceDialog(Service service) {
        EditServiceDialogFragment editDialog = EditServiceDialogFragment.newInstance(service);
        editDialog.setOnServiceUpdatedListener(this::fetchServices);
        editDialog.show(getChildFragmentManager(), "EditServiceDialog");
    }

    @Override
    public void onEdit(Service service) {
        showEditServiceDialog(service);
    }

    @Override
    public void onDelete(Service service) {
        confirmDeleteService(service);
    }
}
