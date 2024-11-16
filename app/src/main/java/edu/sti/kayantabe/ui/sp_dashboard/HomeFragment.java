package edu.sti.kayantabe.ui.sp_dashboard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import edu.sti.kayantabe.Booking;
import edu.sti.kayantabe.R;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private HomeAdapter adapter;
    private List<Booking> bookingList;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        bookingList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadBookings(); // Load bookings from Firestore

        adapter = new HomeAdapter(bookingList, getContext());
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void loadBookings() {
        // Assuming you have a way to get the current logged-in service provider's ID
        String currentServiceProviderId = mAuth.getCurrentUser().getUid(); // Replace with actual logic to get the logged-in service provider's ID

        firestore.collection("bookings")
                .whereEqualTo("serviceProviderId", currentServiceProviderId) // Filter by service provider ID

                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var document : queryDocumentSnapshots) {
                        Booking booking = document.toObject(Booking.class);
                        booking.setBookingId(document.getId());

                        // Fetch customer name from the 'users' collection based on customerId
                        String customerId = booking.getCustomerId();
                        firestore.collection("users").document(customerId)
                                .get()
                                .addOnSuccessListener(userDocument -> {
                                    if (userDocument.exists()) {
                                        String customerName = userDocument.getString("name");
                                        booking.setCustomerName(customerName); // Assuming there's a customerName setter
                                        bookingList.add(booking);
                                        adapter.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure in fetching customer name
                                });

                        // Fetch service image if available
                        if (booking.getImageUrl() != null) {
                            Bitmap bitmap = decodeBase64ToBitmap(booking.getImageUrl());
                            // Assume you have a method to set the image in the view (e.g., adapter or view holder)
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure in loading bookings
                });
    }


    private Bitmap decodeBase64ToBitmap(String base64String) {
        try {
            byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

}
