package com.warisango.dto;

import java.util.ArrayList;
import java.util.List;

public class HeritageBusinessDTO {
    private String businessId;
    private String name;
    private String address;
    private String state;
    private String city;
    private String description;
    private String category;
    private List<String> photos = new ArrayList<>();
    private double latitude;
    private double longitude;
    private Double averageRating;

    public HeritageBusinessDTO() {}

    public HeritageBusinessDTO(String businessId, String name, String address, String state, String city,
                               String description, double latitude, double longitude, Double averageRating) {
        this.businessId = businessId;
        this.name = name;
        this.address = address;
        this.state = state;
        this.city = city;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.averageRating = averageRating;
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

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) {
        this.photos = photos == null ? new ArrayList<>() : new ArrayList<>(photos);
    }
}
