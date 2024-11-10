package edu.sti.kayantabe.ui.sp_services;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import edu.sti.kayantabe.R;
import edu.sti.kayantabe.Service;

public class AddServiceDialogFragment extends DialogFragment {

    private EditText editTextServiceName;
    private EditText editTextServiceDescription;
    private EditText editTextServicePrice;
    private FirebaseFirestore db;
    private OnServiceAddedListener listener;

    public interface OnServiceAddedListener {
        void onServiceAdded();
    }

    public void setOnServiceAddedListener(OnServiceAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_service, container, false);

        editTextServiceName = view.findViewById(R.id.editTextServiceName);
        editTextServiceDescription = view.findViewById(R.id.editTextServiceDescription);
        editTextServicePrice = view.findViewById(R.id.editTextServicePrice);
        Button btnSaveService = view.findViewById(R.id.btnSaveService);

        db = FirebaseFirestore.getInstance();

        btnSaveService.setOnClickListener(v -> {
            String name = editTextServiceName.getText().toString().trim();
            String description = editTextServiceDescription.getText().toString().trim();
            String priceStr = editTextServicePrice.getText().toString().trim();

            if (!name.isEmpty() && !description.isEmpty() && !priceStr.isEmpty()) {
                double price = Double.parseDouble(priceStr);

                // Get the current user's ID
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String userId = user != null ? user.getUid() : null;

                if (userId != null) {
                    // Create a new service object with the userId
                    Service newService = new Service(name, description, price, userId);

                    // Save the new service to Firestore
                    db.collection("services")
                            .add(newService)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(getActivity(), "Service added", Toast.LENGTH_SHORT).show();
                                if (listener != null) {
                                    listener.onServiceAdded();  // Notify the fragment to refresh
                                }
                                dismiss();  // Close dialog
                            })
                            .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to add service", Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(getActivity(), "User not authenticated", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
