package edu.sti.kayantabe;

import java.io.Serializable;

public class Service implements Serializable {

    private String name;
    private String description;
    private double price;
    private String userId;
    private String imageUrl;  // URL for the image associated with the service
    private String id;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
