package edu.sti.kayantabe.ui.sp_employees;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import edu.sti.kayantabe.databinding.FragmentEmployeesBinding;

public class EmployeesFragment extends Fragment {

    private FragmentEmployeesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        EmployeesViewModel employeesViewModel =
                new ViewModelProvider(this).get(EmployeesViewModel.class);

        binding = FragmentEmployeesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textSlideshow;
        employeesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}