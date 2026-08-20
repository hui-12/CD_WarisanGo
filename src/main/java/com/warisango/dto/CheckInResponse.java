package com.warisango.dto;

public class CheckInResponse {
    private boolean success;
    private String message;
    private int pointsEarned;
    private int currentPoints;
    private double distanceMeters;

    public CheckInResponse() {}

    public CheckInResponse(boolean success, String message, int pointsEarned,
                           int currentPoints, double distanceMeters) {
        this.success = success;
        this.message = message;
        this.pointsEarned = pointsEarned;
        this.currentPoints = currentPoints;
        this.distanceMeters = distanceMeters;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(int pointsEarned) { this.pointsEarned = pointsEarned; }

    public int getCurrentPoints() { return currentPoints; }
    public void setCurrentPoints(int currentPoints) { this.currentPoints = currentPoints; }

    public double getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(double distanceMeters) { this.distanceMeters = distanceMeters; }
}
