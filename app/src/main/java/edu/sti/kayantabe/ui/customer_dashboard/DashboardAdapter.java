package edu.sti.kayantabe.ui.customer_dashboard;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.sti.kayantabe.R;
import edu.sti.kayantabe.ServiceWithProvider;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.DashboardViewHolder> {
    private List<ServiceWithProvider> serviceList;
    private Context context;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    public DashboardAdapter(List<ServiceWithProvider> serviceList, Context context) {
        this.serviceList = serviceList;
        this.context = context;
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public DashboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cust_item_service, parent, false);
        return new DashboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardViewHolder holder, int position) {
        ServiceWithProvider service = serviceList.get(position);

        // Set data to the views
        holder.businessName.setText(service.getBusinessName());
        holder.location.setText(service.getBusinessAddress() + ", " + service.getBusinessBarangay());
        holder.serviceType.setText(service.getServiceName());
        holder.price.setText(String.format("PHP %.2f", service.getPrice()));

        // Convert Base64 image string to Bitmap and set it to ImageView
        if (service.getImageUrl() != null) {
            Bitmap bitmap = decodeBase64ToBitmap(service.getImageUrl());
            holder.serviceImage.setImageBitmap(bitmap);
        }

        // Handle item click to show dialog
        holder.itemView.setOnClickListener(v -> showServiceDialog(service));
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class DashboardViewHolder extends RecyclerView.ViewHolder {
        TextView businessName, location, serviceType, price;
        ImageView serviceImage;

        public DashboardViewHolder(View itemView) {
            super(itemView);
            businessName = itemView.findViewById(R.id.business_name);
            location = itemView.findViewById(R.id.location);
            serviceType = itemView.findViewById(R.id.service_type);
            price = itemView.findViewById(R.id.price);
            serviceImage = itemView.findViewById(R.id.service_image);
        }
    }

    private void showServiceDialog(ServiceWithProvider service) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Service Details");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.activity_cust_dialog_service_details, null);
        ImageView serviceImage = dialogView.findViewById(R.id.service_image);
        TextView businessName = dialogView.findViewById(R.id.business_name);
        TextView location = dialogView.findViewById(R.id.location);
        TextView serviceType = dialogView.findViewById(R.id.service_type);
        TextView description = dialogView.findViewById(R.id.description);
        TextView price = dialogView.findViewById(R.id.price);

        if (service.getImageUrl() != null) {
            Bitmap bitmap = decodeBase64ToBitmap(service.getImageUrl());
            serviceImage.setImageBitmap(bitmap);
        }
        businessName.setText(service.getBusinessName());
        location.setText(service.getBusinessAddress() + ", " + service.getBusinessBarangay());
        serviceType.setText(service.getServiceName());
        description.setText(service.getDescription());
        price.setText(String.format("PHP %.2f", service.getPrice()));

        builder.setView(dialogView)
                .setPositiveButton("Book Service", (dialog, which) -> showBookingDateDialog(service))
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void showBookingDateDialog(ServiceWithProvider service) {
        Calendar calendar = Calendar.getInstance();
        // Set minimum date to today
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            showBookingTimeDialog(service, calendar);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        // Disable dates before the current date
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showBookingTimeDialog(ServiceWithProvider service, Calendar calendar) {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR_OF_DAY, 2); // Set minimum booking time to two hours from now

        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(context, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            // If selected date is today, ensure the selected time is at least two hours later than the current time
            if (calendar.getTimeInMillis() < now.getTimeInMillis()) {
                Toast.makeText(context, "Please select a time at least 2 hours from now.", Toast.LENGTH_SHORT).show();
                showBookingTimeDialog(service, calendar); // Reopen time picker if time is invalid
            } else {
                saveBooking(service, calendar.getTimeInMillis());
            }
        }, currentHour, currentMinute, false);

        timePickerDialog.show();
    }

    private void saveBooking(ServiceWithProvider service, long timestamp) {
        String customerId = auth.getCurrentUser().getUid();

        // Format the timestamp into a string in 12-hour format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        String formattedDateTime = String.format("%04d-%02d-%02d %02d:%02d %s",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1, // Month is 0-based, add 1
                calendar.get(Calendar.DAY_OF_MONTH),
                (calendar.get(Calendar.HOUR) == 0 ? 12 : calendar.get(Calendar.HOUR)), // Convert hour to 12-hour format
                calendar.get(Calendar.MINUTE),
                (calendar.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM")); // Append AM/PM

        // Create booking data map
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("customerId", customerId);
        bookingData.put("serviceId", service.getServiceId());
        bookingData.put("serviceProviderId", service.getServiceProviderId());
        bookingData.put("serviceName", service.getServiceName());
        bookingData.put("businessName", service.getBusinessName());
        bookingData.put("bookingDateTime", formattedDateTime); // Save formatted date-time string
        bookingData.put("status", "pending");

        // Save to Firestore
        firestore.collection("bookings").add(bookingData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Booking requested successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to request booking.", Toast.LENGTH_SHORT).show();
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
