package edu.sti.kayantabe.ui.admin_applicants;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.sti.kayantabe.Applicant;

public class ApplicantsViewModel extends ViewModel {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private MutableLiveData<List<Applicant>> applicantsLiveData;

    public LiveData<List<Applicant>> getApplicantsLiveData() {
        if (applicantsLiveData == null) {
            applicantsLiveData = new MutableLiveData<>();
            fetchApplicants();
        }
        return applicantsLiveData;
    }

    public void fetchApplicants() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query the 'users' collection to get applicants with isApproved = false or null and isBusinessPermitComplete = true
        db.collection("users")
                .whereEqualTo("isBusinessDetailsComplete", true)  // Business permit should be complete
                .whereIn("isApproved", Arrays.asList(false, null)) // isApproved should be false or null
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Applicant> applicants = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Get the applicantId (document ID) and fetch service provider details
                        String applicantId = document.getId();

                        // Fetch the corresponding service_provider document using applicantId
                        db.collection("service_providers")
                                .document(applicantId)
                                .get()
                                .addOnSuccessListener(serviceProviderDocument -> {
                                    if (serviceProviderDocument.exists()) {
                                        // Create the Applicant object from the service provider data
                                        Applicant applicant = serviceProviderDocument.toObject(Applicant.class);
                                        // Set the applicantId to the service provider document ID
                                        applicant.setApplicantId(applicantId);

                                        // Add the applicant to the list
                                        applicants.add(applicant);

                                        // Set the updated list to LiveData
                                        applicantsLiveData.setValue(applicants);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ApplicantsViewModel", "Error fetching service provider details: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ApplicantsViewModel", "Error fetching applicants: " + e.getMessage());
                });
    }

}
