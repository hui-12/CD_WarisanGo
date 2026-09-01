package com.warisango.dto;

public class BusinessCorrectionRequest {
    private String name;
    private String address;
    private String city;
    private String state;
    private String operatingHour;
    private String latitude;
    private String longitude;
    private String resolutionNote;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getOperatingHour() { return operatingHour; }
    public void setOperatingHour(String operatingHour) { this.operatingHour = operatingHour; }
    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }
    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }
    public String getResolutionNote() { return resolutionNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }
}
