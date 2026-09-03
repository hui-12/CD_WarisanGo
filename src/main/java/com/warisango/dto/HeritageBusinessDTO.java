package com.warisango.dto;

import java.util.ArrayList;
import java.time.Instant;
import java.util.List;

public class HeritageBusinessDTO {
    private String businessId;
    private String name;
    private String address;
    private String state;
    private String city;
    private String description;
    private String operatingHour;
    private List<String> imageUrls = new ArrayList<>();
    private double latitude;
    private double longitude;
    private Double averageRating;
    private int checkInPoints;
    private String sourceVideoLink;
    private Instant createdAt;
    private Instant approveAt;
    private Instant rejectedAt;

    public HeritageBusinessDTO() {}

    public HeritageBusinessDTO(String businessId, String name, String address, String state, String city,
                               String description, double latitude, double longitude,
                               Double averageRating, int checkInPoints) {
        this.businessId = businessId;
        this.name = name;
        this.address = address;
        this.state = state;
        this.city = city;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.averageRating = averageRating;
        this.checkInPoints = checkInPoints;
    }

    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOperatingHour() { return operatingHour; }
    public void setOperatingHour(String operatingHour) { this.operatingHour = operatingHour; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls == null ? new ArrayList<>() : new ArrayList<>(imageUrls);
    }

    public int getCheckInPoints() { return checkInPoints; }
    public void setCheckInPoints(int checkInPoints) { this.checkInPoints = checkInPoints; }

    public String getSourceVideoLink() { return sourceVideoLink; }
    public void setSourceVideoLink(String sourceVideoLink) { this.sourceVideoLink = sourceVideoLink; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getApproveAt() { return approveAt; }
    public void setApproveAt(Instant approveAt) { this.approveAt = approveAt; }

    public Instant getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(Instant rejectedAt) { this.rejectedAt = rejectedAt; }
}
