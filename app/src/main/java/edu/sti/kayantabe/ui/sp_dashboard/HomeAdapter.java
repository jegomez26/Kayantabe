package edu.sti.kayantabe.ui.sp_dashboard;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.sti.kayantabe.Booking;
import edu.sti.kayantabe.Employee;
import edu.sti.kayantabe.R;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {
    private List<Booking> bookingList;
    private Context context;
    private FirebaseFirestore firestore;

    public HomeAdapter(List<Booking> bookingList, Context context) {
        this.bookingList = bookingList;
        this.context = context;
        firestore = FirebaseFirestore.getInstance();
        // Sort bookings by date and time (latest first)
        Collections.sort(this.bookingList, (b1, b2) -> b2.getBookingDateTime().compareTo(b1.getBookingDateTime()));
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sp_booking_card_item, parent, false);
        return new HomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        holder.serviceName.setText(booking.getServiceName());
//        holder.serviceProvider.setText(booking.getServiceProviderName());
        // Handle bookingDateTime stored as a String
        if (booking.getBookingDateTime() != null && !booking.getBookingDateTime().isEmpty()) {
            holder.bookingDateTime.setText("Booking Date & Time: " + booking.getBookingDateTime());
        } else {
            holder.bookingDateTime.setText("Booking Date & Time: Not Available");
            Log.e("HomeAdapter", "Booking date & time is null or empty for booking ID: " + booking.getBookingId());
        }

        // Display customer name
        firestore.collection("users").document(booking.getCustomerId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String customerName = documentSnapshot.getString("name");
                        holder.customerName.setText("Customer Name: " + customerName);
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

        // Set the image if available
        // Fetch service image from the "services" collection
        if (booking.getServiceId() != null) {
            firestore.collection("services").document(booking.getServiceId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String imageUrl = documentSnapshot.getString("imageUrl"); // Assuming imageUrl is a String
                            if (imageUrl != null) {
                                Bitmap bitmap = decodeBase64ToBitmap(imageUrl);
                                holder.serviceImage.setImageBitmap(bitmap);
                            }

                            holder.price.setText(String.format("PHP %.2f", documentSnapshot.getDouble("price")));
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure in fetching service image
                        Log.e("HomeAdapter", "Error fetching service image", e);
                    });
        }

        // Check the booking status
        if ("pending".equalsIgnoreCase(booking.getStatus())) {
            holder.acceptBooking.setVisibility(View.VISIBLE);
            holder.declineBooking.setVisibility(View.VISIBLE);
            holder.viewAssignedEmployees.setVisibility(View.GONE);

            holder.acceptBooking.setOnClickListener(v -> showEmployeeAssignmentDialog(booking));
            holder.declineBooking.setOnClickListener(v -> declineBooking(booking));
        } else if ("accepted".equalsIgnoreCase(booking.getStatus())
                || "In Transit".equalsIgnoreCase(booking.getStatus())
                || "Arrived".equalsIgnoreCase(booking.getStatus())
                || "Completed".equalsIgnoreCase(booking.getStatus())) {
            holder.acceptBooking.setVisibility(View.GONE);
            holder.declineBooking.setVisibility(View.GONE);
            holder.viewAssignedEmployees.setVisibility(View.VISIBLE);

            holder.viewAssignedEmployees.setOnClickListener(v -> showAssignedEmployeesDialog(booking));
        } else {
            holder.acceptBooking.setVisibility(View.GONE);
            holder.declineBooking.setVisibility(View.GONE);
            holder.viewAssignedEmployees.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class HomeViewHolder extends RecyclerView.ViewHolder {
        TextView serviceName, customerName, bookingDateTime, price, serviceProvider, customerAddress;
        ImageView serviceImage;
        Button acceptBooking, declineBooking, viewAssignedEmployees;

        public HomeViewHolder(View itemView) {
            super(itemView);
            serviceImage = itemView.findViewById(R.id.service_image);
            serviceName = itemView.findViewById(R.id.service_name);
            customerName = itemView.findViewById(R.id.customer_name);
            customerAddress = itemView.findViewById(R.id.customer_address);
            bookingDateTime = itemView.findViewById(R.id.booking_datetime);
            price = itemView.findViewById(R.id.price);
//            serviceProvider = itemView.findViewById(R.id.service_provider);
            acceptBooking = itemView.findViewById(R.id.accept_booking);
            declineBooking = itemView.findViewById(R.id.decline_booking);
            viewAssignedEmployees = itemView.findViewById(R.id.view_assigned_employees);
        }
    }

    private Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    private void showEmployeeAssignmentDialog(Booking booking) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Assign Employees");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_assign_employees, null);

        LinearLayout spinnerContainer = dialogView.findViewById(R.id.spinner_container);
        Button addSpinnerButton = dialogView.findViewById(R.id.add_spinner_button);

        List<Employee> employeeList = new ArrayList<>();
        ArrayAdapter<Employee> adapter = new ArrayAdapter<Employee>(context, android.R.layout.simple_spinner_item, employeeList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setText(employeeList.get(position).toString());
                textView.setPadding(16, 16, 16, 16); // Add padding to the text
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                textView.setText(employeeList.get(position).toString());
                textView.setPadding(16, 16, 16, 16); // Add padding to dropdown items
                return textView;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        firestore.collection("employees")
                .whereEqualTo("serviceProviderId", booking.getServiceProviderId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Employee employee = document.toObject(Employee.class);
                        employeeList.add(employee);
                    }

                    if (employeeList.size() > 1) {
                        addSpinnerButton.setVisibility(View.VISIBLE); // Show button if more than 1 employee
                    } else {
                        addSpinnerButton.setVisibility(View.GONE); // Hide if only 1 employee
                    }

                    addEmployeeSpinner(spinnerContainer, adapter, employeeList, null);
                });

        addSpinnerButton.setOnClickListener(v -> {
            addEmployeeSpinner(spinnerContainer, adapter, employeeList, getSelectedEmployees(spinnerContainer));
        });

        builder.setView(dialogView)
                .setPositiveButton("Assign", (dialog, which) -> {
                    List<String> assignedEmployeeIds = getSelectedEmployees(spinnerContainer);
                    firestore.collection("bookings").document(booking.getBookingId())
                            .update("status", "accepted", "assignedEmployees", assignedEmployeeIds)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Employees assigned and booking accepted.", Toast.LENGTH_SHORT).show();
                                booking.setStatus("accepted");
                                booking.setAssignedEmployees(assignedEmployeeIds);
                                notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to assign employees.", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void showAssignedEmployeesDialog(Booking booking) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Assigned Employees");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_view_assigned_employees, null);
        RecyclerView employeesRecyclerView = dialogView.findViewById(R.id.assigned_employees_recycler_view);

        // Set up RecyclerView
        employeesRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        List<Employee> assignedEmployeesList = new ArrayList<>();
        AssignedEmployeesAdapter adapter = new AssignedEmployeesAdapter(assignedEmployeesList);
        employeesRecyclerView.setAdapter(adapter);

        // Fetch assigned employees from Firestore
        firestore.collection("employees")
                .whereIn("employeeId", booking.getAssignedEmployees())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Employee employee = document.toObject(Employee.class);
                        assignedEmployeesList.add(employee);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to fetch assigned employees.", Toast.LENGTH_SHORT).show();
                });

        builder.setView(dialogView)
                .setPositiveButton("Close", null)
                .create()
                .show();
    }


    // Helper method to dynamically add spinners
    private void addEmployeeSpinner(LinearLayout spinnerContainer, ArrayAdapter<Employee> adapter, List<Employee> employeeList, List<String> selectedEmployeeIds) {
        Spinner spinner = new Spinner(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 16, 0, 16); // Add spacing between spinners
        spinner.setLayoutParams(layoutParams);

        // Create filtered adapter to exclude already selected employees
        List<Employee> availableEmployees = new ArrayList<>(employeeList);
        if (selectedEmployeeIds != null) {
            availableEmployees.removeIf(employee -> selectedEmployeeIds.contains(employee.getEmployeeId()));
        }
        ArrayAdapter<Employee> filteredAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, availableEmployees);
        filteredAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(filteredAdapter);

        spinnerContainer.addView(spinner);
    }

    // Helper method to get selected employee IDs
    private List<String> getSelectedEmployees(LinearLayout spinnerContainer) {
        List<String> selectedEmployeeIds = new ArrayList<>();
        for (int i = 0; i < spinnerContainer.getChildCount(); i++) {
            Spinner spinner = (Spinner) spinnerContainer.getChildAt(i);
            Employee selectedEmployee = (Employee) spinner.getSelectedItem();
            if (selectedEmployee != null) {
                selectedEmployeeIds.add(selectedEmployee.getEmployeeId());
            }
        }
        return selectedEmployeeIds;
    }



    private void declineBooking(Booking booking) {
        firestore.collection("bookings").document(booking.getBookingId())
                .update("status", "declined")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Booking declined.", Toast.LENGTH_SHORT).show();
                    booking.setStatus("declined");
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to decline booking.", Toast.LENGTH_SHORT).show();
                });
    }
}
