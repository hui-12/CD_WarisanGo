package com.warisango.model;

import com.google.cloud.Timestamp;

public class SavedListing {
    private String savedListingId;
    private String touristId;
    private String businessId;
    private Timestamp savedAt;

    public SavedListing() {
    }

    public String getSavedListingId() {
        return savedListingId;
    }

    public void setSavedListingId(String savedListingId) {
        this.savedListingId = savedListingId;
    }

    public String getTouristId() {
        return touristId;
    }

    public void setTouristId(String touristId) {
        this.touristId = touristId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public Timestamp getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(Timestamp savedAt) {
        this.savedAt = savedAt;
    }
}
