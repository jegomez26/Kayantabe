package edu.sti.kayantabe;

import java.util.List;

public class BusinessDetails {

    private String businessName;
    private String barangay;
    private String businessAddress;
    private List<String> services;
    private String businessPermitUrl;
    private String logoUrl;

    // Constructor
    public BusinessDetails(String businessName, String barangay, String businessAddress, List<String> services, String businessPermitUrl, String logoUrl) {
        this.businessName = businessName;
        this.barangay = barangay;
        this.businessAddress = businessAddress;
        this.services = services;
        this.businessPermitUrl = businessPermitUrl;
        this.logoUrl = logoUrl;
    }


    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }


    // Getter and Setter methods
    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBarangay() {
        return barangay;
    }

    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public String getBusinessPermitUrl() {
        return businessPermitUrl;
    }

    public void setBusinessPermitUrl(String businessPermitUrl) {
        this.businessPermitUrl = businessPermitUrl;
    }
}

