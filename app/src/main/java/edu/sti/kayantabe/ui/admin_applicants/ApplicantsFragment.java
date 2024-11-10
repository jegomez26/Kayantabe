package edu.sti.kayantabe.ui.admin_applicants;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import edu.sti.kayantabe.Applicant;
import edu.sti.kayantabe.R;
import edu.sti.kayantabe.databinding.FragmentApplicantsBinding;

public class ApplicantsFragment extends Fragment{

    private FragmentApplicantsBinding binding;
    private ApplicantsViewModel applicantsViewModel;
    private RecyclerView recyclerView;
    private ApplicantsAdapter applicantsAdapter;
    private List<Applicant> applicants;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_applicants, container, false);

        // Initialize RecyclerView and Adapter
        recyclerView = view.findViewById(R.id.applicantsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize applicants list and adapter
        applicants = new ArrayList<>();
        applicantsAdapter = new ApplicantsAdapter(getContext(), applicants);
        recyclerView.setAdapter(applicantsAdapter);

        // Initialize the ViewModel and observe the applicants data
        applicantsViewModel = new ViewModelProvider(this).get(ApplicantsViewModel.class);
        applicantsViewModel.getApplicantsLiveData().observe(getViewLifecycleOwner(), applicants -> {
            if (applicants != null) {
                // Update the adapter with new data
                applicantsAdapter.updateApplicantsList(applicants);
            }
        });

        // Fetch the applicants data from Firestore
        applicantsViewModel.getApplicantsLiveData(); // This should fetch the data from Firestore

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
