package edu.sti.kayantabe.ui.customer_dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import edu.sti.kayantabe.R;
import edu.sti.kayantabe.ServiceWithProvider;
import edu.sti.kayantabe.databinding.FragmentCustDashboardBinding;

import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentCustDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Initialize the ViewModel
        DashboardViewModel dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // Inflate the layout and get the root view
        binding = FragmentCustDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize RecyclerView
        RecyclerView recyclerView = binding.recyclerViewServices; // Assuming RecyclerView is defined in the layout XML
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Observe the service data from the ViewModel
        dashboardViewModel.getServices().observe(getViewLifecycleOwner(), new Observer<List<ServiceWithProvider>>() {
            @Override
            public void onChanged(List<ServiceWithProvider> services) {
                // Set up the adapter when the data changes
                DashboardAdapter adapter = new DashboardAdapter(services, getContext());
                recyclerView.setAdapter(adapter);
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
