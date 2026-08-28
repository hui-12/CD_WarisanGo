package com.warisango.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;

public class Business {
    private String businessId;
    private String name;
    private String state;
    private String city;
    private Double averageRating;
    private String address;
    private String description;
    private String operatingHour;
    private GeoPoint location;
    private int checkInPoints;
    private String sourceVideoLink;
    private String status;
    private Timestamp createdAt;
    private Timestamp approveAt;
    private Timestamp rejectedAt;

    public Business() {}

    public Business(String businessId, String name, String state, String city,
                    Double averageRating, String address, String description) {
        this.businessId = businessId;
        this.name = name;
        this.state = state;
        this.city = city;
        this.averageRating = averageRating;
        this.address = address;
        this.description = description;
    }

    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getOperatingHour() { return operatingHour; }
    public void setOperatingHour(String operatingHour) { this.operatingHour = operatingHour; }
    public GeoPoint getLocation() { return location; }
    public void setLocation(GeoPoint location) { this.location = location; }
    public int getCheckInPoints() { return checkInPoints; }
    public void setCheckInPoints(int checkInPoints) { this.checkInPoints = checkInPoints; }
    public String getSourceVideoLink() { return sourceVideoLink; }
    public void setSourceVideoLink(String sourceVideoLink) { this.sourceVideoLink = sourceVideoLink; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getApproveAt() { return approveAt; }
    public void setApproveAt(Timestamp approveAt) { this.approveAt = approveAt; }
    public Timestamp getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(Timestamp rejectedAt) { this.rejectedAt = rejectedAt; }
}
