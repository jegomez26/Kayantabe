package edu.sti.kayantabe.ui.customer_dashboard;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

import edu.sti.kayantabe.R;
import edu.sti.kayantabe.ServiceWithProvider;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.DashboardViewHolder> {
    private List<ServiceWithProvider> serviceList;
    private Context context;

    public DashboardAdapter(List<ServiceWithProvider> serviceList, Context context) {
        this.serviceList = serviceList;
        this.context = context;
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
        holder.price.setText(String.format("$%.2f", service.getPrice()));

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

    // ViewHolder class to hold the UI components
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
        // Create and show a dialog displaying the full service details
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(service.getServiceName());

        View dialogView = LayoutInflater.from(context).inflate(R.layout.activity_cust_dialog_service_details, null);
        ImageView serviceImage = dialogView.findViewById(R.id.service_image);
        TextView businessName = dialogView.findViewById(R.id.business_name);
        TextView location = dialogView.findViewById(R.id.location);
        TextView serviceType = dialogView.findViewById(R.id.service_type);
        TextView description = dialogView.findViewById(R.id.description);
        TextView price = dialogView.findViewById(R.id.price);

        // Set data to the dialog views
        if (service.getImageUrl() != null) {
            Bitmap bitmap = decodeBase64ToBitmap(service.getImageUrl());
            serviceImage.setImageBitmap(bitmap);
        }
        businessName.setText(service.getBusinessName());
        location.setText(service.getBusinessAddress() + ", " + service.getBusinessBarangay());
        serviceType.setText(service.getServiceName());
        description.setText(service.getDescription());
        price.setText(String.format("$%.2f", service.getPrice()));

        // Add booking button
        builder.setView(dialogView)
                .setPositiveButton("Book Service", (dialog, which) -> showBookingDateDialog())
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void showBookingDateDialog() {
        // Show date picker dialog for booking
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year1, month1, dayOfMonth) -> {
            // Handle date selected
        }, year, month, day);
        datePickerDialog.show();
    }

    // Method to decode Base64 string to Bitmap
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
