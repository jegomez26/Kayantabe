package edu.sti.kayantabe;

public class ServiceProvider {
    private String businessName;
    private String representativeName;
    private String location;
    private String servicesOffered;
    private String businessPermitUrl;
    private String status;

    // Default constructor for Firestore deserialization
    public ServiceProvider() {
    }

    // Constructor to initialize the fields
    public ServiceProvider(String businessName, String representativeName, String location,
                           String servicesOffered, String businessPermitUrl, String status) {
        this.businessName = businessName;
        this.representativeName = representativeName;
        this.location = location;
        this.servicesOffered = servicesOffered;
        this.businessPermitUrl = businessPermitUrl;
        this.status = status;
    }

    // Getters and Setters
    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getRepresentativeName() {
        return representativeName;
    }

    public void setRepresentativeName(String representativeName) {
        this.representativeName = representativeName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getServicesOffered() {
        return servicesOffered;
    }

    public void setServicesOffered(String servicesOffered) {
        this.servicesOffered = servicesOffered;
    }
}


