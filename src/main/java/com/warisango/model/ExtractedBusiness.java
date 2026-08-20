package com.warisango.model;

public class ExtractedBusiness {

    private String name;
    private String address;
    private String state;
    private String city;
    private String location;
    private String description;
    private String operatingHour;

    public ExtractedBusiness() {
    }

    public ExtractedBusiness(
            String name,
            String address,
            String state,
            String city,
            String location,
            String description,
            String operatingHour) {

        this.name = name;
        this.address = address;
        this.state = state;
        this.city = city;
        this.location = location;
        this.description = description;
        this.operatingHour = operatingHour;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getState() {
        return state;
    }

    public String getCity() {
        return city;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getOperatingHour() {
        return operatingHour;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOperatingHour(String operatingHour) {
        this.operatingHour = operatingHour;
    }
}
