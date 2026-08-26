package com.warisango.dto;

public class UserPointsDTO {
    private String userId;
    private int currentPoints;

    public UserPointsDTO() {}

    public UserPointsDTO(String userId, int currentPoints) {
        this.userId = userId;
        this.currentPoints = currentPoints;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getCurrentPoints() { return currentPoints; }
    public void setCurrentPoints(int currentPoints) { this.currentPoints = currentPoints; }
}
