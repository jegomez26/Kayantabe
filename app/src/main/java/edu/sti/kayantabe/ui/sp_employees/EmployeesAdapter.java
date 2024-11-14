package edu.sti.kayantabe.ui.sp_employees;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.util.List;

import edu.sti.kayantabe.Employee;
import edu.sti.kayantabe.R;

public class EmployeesAdapter extends RecyclerView.Adapter<EmployeesAdapter.EmployeeViewHolder> {

    private List<Employee> employeeList;
    private Context context;

    public EmployeesAdapter(List<Employee> employeeList) {
        this.employeeList = employeeList;
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_employee, parent, false);
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {

        Employee employee = employeeList.get(position);
        holder.tvEmployeeName.setText(employee.getFirstName() + " " + employee.getLastName());
        holder.tvContactNumber.setText(employee.getContactNumber());
        // Set the employee email
        if (employee.getEmail() != null) {
            holder.tvEmail.setText(employee.getEmail());
        } else {
            holder.tvEmail.setText("Email not available"); // Default text if email is missing
        }

        // Decode the employee's image from Base64 and set it in the ImageView
        if (employee.getImage() != null && !employee.getImage().isEmpty()) {
            Bitmap bitmap = decodeBase64ToBitmap(employee.getImage());
            holder.ivProfilePicture.setImageBitmap(bitmap);
        } else {
            holder.ivProfilePicture.setImageResource(R.drawable.ic_image); // Default profile image
        }
    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    // Helper method to decode and compress Base64 string to a Bitmap
    private Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);

        // Decode the image with an inSampleSize to reduce resolution
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

        // Calculate inSampleSize (adjust this as needed for desired quality and size)
        options.inSampleSize = calculateInSampleSize(options, 100, 100); // Set target width and height
        options.inJustDecodeBounds = false;

        // Decode the image with the calculated inSampleSize
        Bitmap scaledBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

        // Compress the bitmap if necessary
        return compressBitmap(scaledBitmap);
    }

    public static class EmployeeViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivProfilePicture;
        private TextView tvEmployeeName, tvContactNumber, tvEmail;

        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfilePicture = itemView.findViewById(R.id.ivProfilePicture);
            tvEmployeeName = itemView.findViewById(R.id.tvEmployeeName);
            tvContactNumber = itemView.findViewById(R.id.tvContactNumber);
            tvEmail = itemView.findViewById(R.id.tvEmail);
        }
    }

    // Helper method to compress the Bitmap
    private Bitmap compressBitmap(Bitmap bitmap) {
        // Optional compression if additional reduction in file size is needed
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out); // 70% quality (adjust as needed)
        byte[] byteArray = out.toByteArray();

        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    // Helper method to calculate inSampleSize for BitmapFactory options
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
