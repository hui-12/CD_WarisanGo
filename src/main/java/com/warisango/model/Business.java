package com.warisango.model;

import java.util.List;

public class Business {
    private String id;
    private String name;
    private String category;
    private String state;
    private String city;
    private double rating;
    private String address;
    private String description;
    private List<String> photos;

    public Business() {}

    public Business(String id, String name, String category, String state, String city, double rating, String address, String description, List<String> photos) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.state = state;
        this.city = city;
        this.rating = rating;
        this.address = address;
        this.description = description;
        this.photos = photos;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) { this.photos = photos; }
}
