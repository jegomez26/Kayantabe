package edu.sti.kayantabe.ui.emp_assigned_bookings;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.sti.kayantabe.Booking;
import edu.sti.kayantabe.R;

public class CurrentAssignedBookingsFragment extends Fragment {

    private RecyclerView recyclerView;
    private BookingsAdapter adapter;
    private List<Booking> bookingList = new ArrayList<>();
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_assigned_bookings, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BookingsAdapter(bookingList);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        loadCurrentBookings();

        return view;
    }

    private void loadCurrentBookings() {
        // Fetch bookings with status "accepted", "in transit", "arrived", and "completed"
        firestore.collection("bookings")
                .whereIn("status", new ArrayList<String>() {{
                    add("accepted");
                    add("In Transit");
                    add("Arrived");
                    add("Completed");
                }})
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    bookingList.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Booking booking = document.toObject(Booking.class);

                        // Set the document ID as the bookingId
                        booking.setBookingId(document.getId());

                        bookingList.add(booking);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load bookings.", Toast.LENGTH_SHORT).show();
                });
    }
}
