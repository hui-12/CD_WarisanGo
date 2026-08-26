package com.warisango.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class HeritageBusinessUpdateRequest {

    @NotBlank(message = "Business name is required.")
    @Size(max = 150, message = "Business name must not exceed 150 characters.")
    private String name;

    @Size(max = 300, message = "Address must not exceed 300 characters.")
    private String address;

    @Size(max = 100, message = "State must not exceed 100 characters.")
    private String state;

    @Size(max = 100, message = "City must not exceed 100 characters.")
    private String city;

    @Size(max = 5000, message = "Description must not exceed 5000 characters.")
    private String description;

    @DecimalMin(value = "-90.0", message = "Latitude must be at least -90.")
    @DecimalMax(value = "90.0", message = "Latitude must not exceed 90.")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be at least -180.")
    @DecimalMax(value = "180.0", message = "Longitude must not exceed 180.")
    private Double longitude;

    @Size(max = 100, message = "Operating hours must not exceed 100 characters.")
    private String operatingHour;

    @DecimalMin(value = "0.0", message = "Average rating must be at least 0.")
    @DecimalMax(value = "5.0", message = "Average rating must not exceed 5.")
    private Double averageRating;

    @Min(value = 1, message = "Check-in points must be at least 1.")
    @Max(value = 10000, message = "Check-in points must not exceed 10000.")
    private Integer checkInPoints;

    @Size(max = 2000, message = "Source video URL must not exceed 2000 characters.")
    private String sourceVideoLink;

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
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getOperatingHour() { return operatingHour; }
    public void setOperatingHour(String operatingHour) { this.operatingHour = operatingHour; }
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    public Integer getCheckInPoints() { return checkInPoints; }
    public void setCheckInPoints(Integer checkInPoints) { this.checkInPoints = checkInPoints; }
    public String getSourceVideoLink() { return sourceVideoLink; }
    public void setSourceVideoLink(String sourceVideoLink) { this.sourceVideoLink = sourceVideoLink; }
}
