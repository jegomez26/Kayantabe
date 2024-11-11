package edu.sti.kayantabe.ui.customer_bookings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import edu.sti.kayantabe.databinding.FragmentCustBookingsBinding;

public class BookingsFragment extends Fragment {

    private FragmentCustBookingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        BookingsViewModel bookingsViewModel =
                new ViewModelProvider(this).get(BookingsViewModel.class);

        binding = FragmentCustBookingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textGallery;
        bookingsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}