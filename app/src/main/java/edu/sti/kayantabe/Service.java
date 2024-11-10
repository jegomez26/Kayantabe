package edu.sti.kayantabe;

public class Service {

    private String name;
    private String description;
    private double price;
    private String userId;  // Add userId to associate service with the service provider

    public Service() {
        // Default constructor required for Firestore serialization
    }

    public Service(String name, String description, double price, String userId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.userId = userId;  // Initialize userId
    }

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
}
