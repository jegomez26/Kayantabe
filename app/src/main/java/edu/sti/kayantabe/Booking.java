package edu.sti.kayantabe;

import java.util.Date;

public class Booking {
    private String bookingId;
    private String customerId;
    private String serviceId;
    private String serviceName;
    private String serviceProviderId;
    private String serviceProviderName;
    private String status;
    private String imageUrl; // URL of the service image
    private double price;
    private String bookingDateTime;

    // Constructors
    public Booking() {}

    public Booking(String bookingId, String customerId, String serviceId, String serviceName,
                   String serviceProviderId, String serviceProviderName, String status,
                   String imageUrl, double price, String bookingDateTime) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.serviceProviderId = serviceProviderId;
        this.serviceProviderName = serviceProviderName;
        this.status = status;
        this.imageUrl = imageUrl;
        this.price = price;
        this.bookingDateTime = bookingDateTime;
    }

    // Getters and Setters
    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

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

    public String getServiceProviderId() {
        return serviceProviderId;
    }

    public void setServiceProviderId(String serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getBookingTimestamp() {
        return bookingDateTime;
    }

    public void setBookingTimestamp(String bookingTimestamp) {
        this.bookingDateTime = bookingTimestamp;
    }
}