package edu.sti.kayantabe.ui.emp_assigned_bookings;

import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import edu.sti.kayantabe.Booking;
import edu.sti.kayantabe.R;

public class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.BookingViewHolder> {

    private List<Booking> bookingList;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public BookingsAdapter(List<Booking> bookingList) {
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.emp_item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        // Display service name and booking date/time
        holder.serviceName.setText(booking.getServiceName());
        holder.bookingDateTime.setText("Booking Date & Time: " + booking.getBookingDateTime());

        // Fetch customer details
        firestore.collection("users").document(booking.getCustomerId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String customerName = documentSnapshot.getString("name");
                        holder.customerName.setText("Customer: " + customerName);
                    }
                });

        firestore.collection("customers").document(booking.getCustomerId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String address = documentSnapshot.getString("otherAddress") + ", " +
                                documentSnapshot.getString("barangay") + ". " +
                                documentSnapshot.getString("city") + ", " +
                                documentSnapshot.getString("province");
                        holder.customerAddress.setText("Customer Address: " + address);
                    }
                });

        // Handle button for status change
        holder.statusButton.setOnClickListener(view -> {
            String currentStatus = booking.getStatus();  // Assuming status is stored in the booking object

            // Show confirmation dialog before updating the status
            showConfirmationDialog(view, currentStatus, booking, holder.statusButton);
        });

        firestore.collection("services").document(booking.getServiceId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        holder.price.setText(String.format("PHP %.2f", documentSnapshot.getDouble("price")));
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure in fetching service image
                    Log.e("HomeAdapter", "Error fetching service price", e);
                });

        // Set button text based on the current status
        setButtonText(holder.statusButton, booking.getStatus());
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {

        TextView serviceName, bookingDateTime, customerName, customerAddress, price;
        Button statusButton;

        public BookingViewHolder(View itemView) {
            super(itemView);
            serviceName = itemView.findViewById(R.id.service_name);
            bookingDateTime = itemView.findViewById(R.id.booking_date_time);
            customerName = itemView.findViewById(R.id.customer_name);
            customerAddress = itemView.findViewById(R.id.customer_address);
            price = itemView.findViewById(R.id.price);
            statusButton = itemView.findViewById(R.id.status_button);
        }
    }

    // Function to update booking status
    private void updateStatus(Booking booking, String newStatus, Button statusButton) {
        String bookingId = booking.getBookingId();

        // Check if bookingId is null or empty before proceeding
        if (bookingId == null || bookingId.isEmpty()) {
            Log.e("BookingsAdapter", "Booking ID is null or empty");
            Toast.makeText(statusButton.getContext(), "Invalid booking ID", Toast.LENGTH_SHORT).show();
            return;  // Exit the function to avoid further errors
        }

        firestore.collection("bookings").document(bookingId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    booking.setStatus(newStatus);  // Update the local object to reflect the new status
                    setButtonText(statusButton, newStatus);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(statusButton.getContext(), "Error updating status", Toast.LENGTH_SHORT).show();
                    Log.e("BookingsAdapter", "Error updating status", e);
                });
    }

    // Function to set the button text based on the booking status
    private void setButtonText(Button statusButton, String status) {
        switch (status) {
            case "In Transit":
                statusButton.setText("Arrived");
                break;
            case "Arrived":
                statusButton.setText("Complete");
                break;
            case "Completed":
                statusButton.setText("Completed");
                statusButton.setEnabled(false);  // Disable the button if completed
                break;
            default:
                statusButton.setText("In Transit");
                break;
        }
    }

    // Function to show confirmation dialog before updating the status
    private void showConfirmationDialog(View view, String currentStatus, Booking booking, Button statusButton) {
        String actionMessage = "";

        // Determine the action message based on the current status
        switch (currentStatus) {
            case "In Transit":
                actionMessage = "Are you sure you want to mark this booking as 'Arrived'?";
                break;
            case "Arrived":
                actionMessage = "Are you sure you want to mark this booking as 'Completed'?";
                break;
            case "Completed":
                Toast.makeText(view.getContext(), "Booking already completed", Toast.LENGTH_SHORT).show();
                return;
            default:
                actionMessage = "Are you sure you want to mark this booking as 'In Transit'?";
                break;
        }

        // Create and show the confirmation dialog
        new AlertDialog.Builder(view.getContext())
                .setMessage(actionMessage)
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> updateStatus(booking, getNextStatus(currentStatus), statusButton))
                .setNegativeButton("No", null)
                .show();
    }

    // Function to determine the next status based on the current status
    private String getNextStatus(String currentStatus) {
        switch (currentStatus) {
            case "In Transit":
                return "Arrived";
            case "Arrived":
                return "Completed";
            case "Completed":
                return "Completed"; // No change as already completed
            default:
                return "In Transit";
        }
    }
}
