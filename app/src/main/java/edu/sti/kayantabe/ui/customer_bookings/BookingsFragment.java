package edu.sti.kayantabe.ui.customer_bookings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.sti.kayantabe.R;
import edu.sti.kayantabe.Booking;

public class BookingsFragment extends Fragment {

    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_cust_bookings, container, false);

        // Initialize RecyclerView and set LayoutManager
        recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Firebase instances and lists
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        bookingList = new ArrayList<>();
        bookingAdapter = new BookingAdapter(bookingList, getContext());
        recyclerView.setAdapter(bookingAdapter);

        // Load bookings from Firestore
        loadBookings();

        return root;
    }

    private void loadBookings() {
        String currentUserId = mAuth.getCurrentUser().getUid();

        firestore.collection("bookings")
                .whereEqualTo("customerId", currentUserId) // Fetch bookings for the current customer
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        bookingList.clear(); // Clear the list to avoid duplication
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Extract booking fields
                            String bookingId = document.getId();
                            String customerId = document.getString("customerId");
                            String serviceId = document.getString("serviceId");
                            String serviceName = document.getString("serviceName");
                            String serviceProviderId = document.getString("serviceProviderId");
                            String serviceProviderName = document.getString("businessName");
                            String status = document.getString("status");

                            // Handle the bookingTimestamp safely
                            String bookingTimestamp = document.getString("bookingDateTime");

                            // Fetch service details (imageUrl and price)
                            fetchServiceDetails(serviceId, booking -> {
                                if (booking != null) {
                                    booking.setBookingId(bookingId);
                                    booking.setCustomerId(customerId);
                                    booking.setServiceId(serviceId);
                                    booking.setServiceName(serviceName);
                                    booking.setServiceProviderId(serviceProviderId);
                                    booking.setServiceProviderName(serviceProviderName);
                                    booking.setStatus(status);
                                    booking.setBookingTimestamp(bookingTimestamp);

                                    bookingList.add(booking);
                                    bookingAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                });
    }

    private void fetchServiceDetails(String serviceId, BookingCallback callback) {
        firestore.collection("services").document(serviceId).get()
                .addOnSuccessListener(serviceDoc -> {
                    if (serviceDoc.exists()) {
                        String imageUrl = serviceDoc.getString("imageUrl");
                        double price = serviceDoc.getDouble("price");

                        Booking booking = new Booking();
                        booking.setImageUrl(imageUrl);
                        booking.setPrice(price);

                        callback.onBookingFetched(booking);
                    } else {
                        callback.onBookingFetched(null);
                    }
                })
                .addOnFailureListener(e -> callback.onBookingFetched(null));
    }

    interface BookingCallback {
        void onBookingFetched(Booking booking);
    }
}
