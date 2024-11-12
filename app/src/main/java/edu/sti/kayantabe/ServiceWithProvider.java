package edu.sti.kayantabe;

public class ServiceWithProvider {
    private String businessName;
    private String businessAddress;
    private String barangay;
    private String serviceName;
    private String description;
    private double price;
    private String imageUrl;

    // Constructor
    public ServiceWithProvider(String businessName, String businessAddress, String businessBarangay,
                               String serviceName, String description, double price, String imageUrl) {
        this.businessName = businessName;
        this.businessAddress = businessAddress;
        this.barangay = businessBarangay;
        this.serviceName = serviceName;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    // Getters and setters
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
        return barangay;
    }

    public void setBusinessBarangay(String businessBarangay) {
        this.barangay = businessBarangay;
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
