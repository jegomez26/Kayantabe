package edu.sti.kayantabe.ui.sp_services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import edu.sti.kayantabe.R;
import edu.sti.kayantabe.Service;

public class ServicesAdapter extends RecyclerView.Adapter<ServicesAdapter.ServiceViewHolder> {

    private ArrayList<Service> servicesList;
    private OnServiceActionListener actionListener;

    public interface OnServiceActionListener {
        void onEdit(Service service);
        void onDelete(Service service);
    }

    public ServicesAdapter(ArrayList<Service> servicesList, OnServiceActionListener actionListener) {
        this.servicesList = servicesList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = servicesList.get(position);
        holder.nameTextView.setText(service.getName());
        holder.descriptionTextView.setText(service.getDescription());
        holder.amountTextView.setText("Price: PHP " + String.format("%.2f", service.getPrice()));

        // Decode Base64 string to Bitmap and set it to ImageView
        if (service.getImageUrl() != null && !service.getImageUrl().isEmpty()) {
            Bitmap decodedImage = decodeBase64ToBitmap(service.getImageUrl());
            holder.serviceImageView.setImageBitmap(decodedImage);
        } else {
            holder.serviceImageView.setImageResource(R.drawable.ic_placeholder);
        }

        // Set listeners for edit and delete buttons
        holder.btnEditService.setOnClickListener(v -> actionListener.onEdit(service));
        holder.btnDeleteService.setOnClickListener(v -> actionListener.onDelete(service));
    }

    @Override
    public int getItemCount() {
        return servicesList.size();
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView descriptionTextView;
        TextView amountTextView;
        ImageView serviceImageView;
        Button btnEditService;
        Button btnDeleteService;

        public ServiceViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.serviceName);
            descriptionTextView = itemView.findViewById(R.id.serviceDescription);
            amountTextView = itemView.findViewById(R.id.serviceAmount);
            serviceImageView = itemView.findViewById(R.id.serviceImage);
            btnEditService = itemView.findViewById(R.id.btn_editService);
            btnDeleteService = itemView.findViewById(R.id.btn_deleteService);
        }
    }

    // Method to decode Base64 string to Bitmap
    private Bitmap decodeBase64ToBitmap(String base64String) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
