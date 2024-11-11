package edu.sti.kayantabe;

public class Service {

    private String name;
    private String description;
    private double price;
    private String userId;
    private String imageUrl;  // URL for the image associated with the service

    public Service() {
        // Default constructor required for Firestore serialization
    }

    public Service(String name, String description, double price, String userId, String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.userId = userId;
        this.imageUrl = imageUrl;  // Initialize imageUrl
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getUserId() {
        return userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
