// Applicant.java
package edu.sti.kayantabe;

import java.util.List;

public class Applicant {
    private String applicantId; // Applicant's unique ID
    private String businessName;
    private String barangay;
    private String businessAddress;
    private List<String> services;
    private String businessPermitUrl; // Base64 string representing the business permit

    // Constructor, getters, and setters
    public Applicant() {
        // Empty constructor for Firestore deserialization
    }

    public Applicant(String applicantId, String businessName, String barangay, String businessAddress, List<String> services, String permitBase64) {
        this.applicantId = applicantId;
        this.businessName = businessName;
        this.barangay = barangay;
        this.businessAddress = businessAddress;
        this.services = services;
        this.businessPermitUrl = permitBase64;
    }

    // Getter and setter for applicantId
    public String getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }

    // Getter and setter for businessName
    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    // Getter and setter for barangay
    public String getBarangay() {
        return barangay;
    }

    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }

    // Getter and setter for businessAddress
    public String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }

    // Getter and setter for services
    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    // Getter and setter for permitBase64
    public String getPermitBase64() {
        return businessPermitUrl;
    }

    public void setPermitBase64(String permitBase64) {
        this.businessPermitUrl = permitBase64;
    }
}
