package edu.sti.kayantabe.ui.customer_dashboard;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.sti.kayantabe.ServiceWithProvider;

import java.util.ArrayList;
import java.util.List;

public class DashboardViewModel extends ViewModel {

    private MutableLiveData<List<ServiceWithProvider>> services;

    public DashboardViewModel() {
        services = new MutableLiveData<>();
        loadServices();
    }

    public LiveData<List<ServiceWithProvider>> getServices() {
        return services;
    }

    private void loadServices() {
        fetchServicesFromDatabase();
    }

    private void fetchServicesFromDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<ServiceWithProvider> serviceWithProviderList = new ArrayList<>();

        db.collection("services").get()
                .addOnSuccessListener(serviceSnapshots -> {
                    int totalServices = serviceSnapshots.size();
                    if (totalServices == 0) {
                        services.setValue(serviceWithProviderList);
                        return;
                    }

                    for (DocumentSnapshot serviceDoc : serviceSnapshots.getDocuments()) {
                        String serviceName = serviceDoc.getString("name");
                        String description = serviceDoc.getString("description");
                        Double price = serviceDoc.getDouble("price");
                        String imageUrl = serviceDoc.getString("imageUrl");
                        String providerId = serviceDoc.getString("userId");

                        if (providerId != null) {
                            db.collection("service_providers").document(providerId).get()
                                    .addOnSuccessListener(providerDoc -> {
                                        String businessName = providerDoc.getString("businessName");
                                        String businessAddress = providerDoc.getString("businessAddress");
                                        String businessBarangay = providerDoc.getString("barangay");

                                        ServiceWithProvider serviceWithProvider = new ServiceWithProvider(
                                                businessName, businessAddress, businessBarangay,
                                                serviceName, description, price != null ? price : 0.0, imageUrl
                                        );

                                        serviceWithProviderList.add(serviceWithProvider);

                                        // Check if we've added all services before setting the value
                                        if (serviceWithProviderList.size() == totalServices) {
                                            services.setValue(new ArrayList<>(serviceWithProviderList));
                                        }

                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("DashboardViewModel", "Error fetching provider details: ", e);
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DashboardViewModel", "Error fetching services: ", e);
                });
    }
}
