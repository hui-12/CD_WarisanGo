package com.warisango.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;

public class CheckIn {
    private String businessId;
    private String businessName;
    private String checkInId;
    private Timestamp checkInTimestamp;
    private GeoPoint gpsLocation;
    private int pointsAwarded;
    private String touristId;

    public CheckIn() {
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getCheckInId() {
        return checkInId;
    }

    public void setCheckInId(String checkInId) {
        this.checkInId = checkInId;
    }

    public Timestamp getCheckInTimestamp() {
        return checkInTimestamp;
    }

    public void setCheckInTimestamp(Timestamp checkInTimestamp) {
        this.checkInTimestamp = checkInTimestamp;
    }

    public GeoPoint getGpsLocation() {
        return gpsLocation;
    }

    public void setGpsLocation(GeoPoint gpsLocation) {
        this.gpsLocation = gpsLocation;
    }

    public int getPointsAwarded() {
        return pointsAwarded;
    }

    public void setPointsAwarded(int pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }

    public String getTouristId() {
        return touristId;
    }

    public void setTouristId(String touristId) {
        this.touristId = touristId;
    }
}
