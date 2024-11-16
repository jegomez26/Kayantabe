package edu.sti.kayantabe.ui.customer_bookings;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import edu.sti.kayantabe.Booking;
import edu.sti.kayantabe.R;
import edu.sti.kayantabe.ServiceWithProvider;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    private List<Booking> bookingList;
    private Context context;
    private FirebaseFirestore firestore;

    public BookingAdapter(List<Booking> bookingList, Context context) {
        this.bookingList = bookingList;
        this.context = context;
        firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.booking_card_item, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        holder.serviceName.setText(booking.getServiceName());
        holder.serviceProvider.setText(booking.getServiceProviderName());
        holder.price.setText(String.format("PHP %.2f", booking.getPrice()));
        holder.bookingDateTime.setText("Booking Date & Time: " + booking.getBookingDateTime());

        holder.bookingStatus.setText("Status: " + booking.getStatus());
        if ("pending".equalsIgnoreCase(booking.getStatus())) {
            holder.cancelBooking.setVisibility(View.VISIBLE);
        } else {
            holder.cancelBooking.setVisibility(View.GONE);
        }

        // Decode Base64 image
        if (booking.getImageUrl() != null) {
            Bitmap bitmap = decodeBase64ToBitmap(booking.getImageUrl());
            holder.serviceImage.setImageBitmap(bitmap);
        }

        // View Booking Details button
        holder.viewBookingDetails.setOnClickListener(v -> showBookingDetailsDialog(booking));

        // Cancel Booking button
        holder.cancelBooking.setOnClickListener(v -> cancelBooking(booking));
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView serviceName, serviceProvider, price, bookingDateTime, bookingStatus;
        ImageView serviceImage;
        Button viewBookingDetails, cancelBooking;

        public BookingViewHolder(View itemView) {
            super(itemView);
            serviceImage = itemView.findViewById(R.id.service_image);
            serviceName = itemView.findViewById(R.id.service_name);
            serviceProvider = itemView.findViewById(R.id.service_provider);
            price = itemView.findViewById(R.id.price);
            bookingDateTime = itemView.findViewById(R.id.booking_datetime);
            bookingStatus = itemView.findViewById(R.id.booking_status);
            viewBookingDetails = itemView.findViewById(R.id.view_booking_details);
            cancelBooking = itemView.findViewById(R.id.cancel_booking);
        }
    }

    private void showBookingDetailsDialog(Booking booking) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Booking Details");

        // Inflate custom dialog layout and populate it with booking details
        View dialogView = LayoutInflater.from(context).inflate(R.layout.booking_details_dialog, null);
        ImageView serviceImage = dialogView.findViewById(R.id.service_image);
        TextView serviceName = dialogView.findViewById(R.id.service_name);
        TextView serviceProvider = dialogView.findViewById(R.id.service_provider_name);
//        TextView description = dialogView.findViewById(R.id.service_description);
        TextView bookingDateTime = dialogView.findViewById(R.id.booking_date_time);
        TextView bookingStatus = dialogView.findViewById(R.id.booking_status);

        // Set the data
        if (booking.getImageUrl() != null) {
            serviceImage.setImageBitmap(decodeBase64ToBitmap(booking.getImageUrl()));
        }
        serviceName.setText(booking.getServiceName());
        serviceProvider.setText(booking.getServiceProviderName());
//        description.setText(booking.getDescription());]
        bookingDateTime.setText(booking.getBookingDateTime());
        bookingStatus.setText(booking.getStatus());

        builder.setView(dialogView)
                .setNegativeButton("Close", null)
                .create()
                .show();
    }

    private void cancelBooking(Booking booking) {
        // Create confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Proceed with cancellation
                    firestore.collection("bookings").document(booking.getBookingId())
                            .update("status", "cancelled")
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Booking cancelled successfully.", Toast.LENGTH_SHORT).show();
                                booking.setStatus("cancelled");
                                notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to cancel booking.", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Dismiss dialog
                    dialog.dismiss();
                })
                .create()
                .show();
    }


    private Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
}
