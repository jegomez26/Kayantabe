package edu.sti.kayantabe;

public class ServiceWithProvider {
    private String serviceId;  // Document ID from Firestore
    private String serviceName;
    private String description;
    private String businessName;
    private String businessAddress;
    private String businessBarangay;
    private String serviceProviderId;
    private double price;
    private String imageUrl;

    // Constructor
    public ServiceWithProvider() {
        // Default constructor required for calls to DataSnapshot.getValue(ServiceWithProvider.class)
    }

    public ServiceWithProvider(String serviceId, String serviceName, String description,
                               String businessName, String businessAddress, String businessBarangay,
                               String serviceProviderId, double price, String imageUrl) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.description = description;
        this.businessName = businessName;
        this.businessAddress = businessAddress;
        this.businessBarangay = businessBarangay;
        this.serviceProviderId = serviceProviderId;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public String getBusinessBarangay() {
        return businessBarangay;
    }

    public void setBusinessBarangay(String businessBarangay) {
        this.businessBarangay = businessBarangay;
    }

    public String getServiceProviderId() {
        return serviceProviderId;
    }

    public void setServiceProviderId(String serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
