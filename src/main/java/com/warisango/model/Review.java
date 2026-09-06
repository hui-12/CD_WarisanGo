package com.warisango.model;

import java.util.ArrayList;
import java.util.List;

/** Represents a document in the Firestore reviews collection. */
public class Review {
    private String reviewId;
    private String businessId;
    private String touristId;
    private String touristName;
    private String reviewText;
    private int rating;
    private String moderationStatus = "VISIBLE";
    private String createdAt;
    private String updatedAt;
    private List<String> photoUrls = new ArrayList<>();

    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }
    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }
    public String getTouristId() { return touristId; }
    public void setTouristId(String touristId) { this.touristId = touristId; }
    public String getTouristName() { return touristName; }
    public void setTouristName(String touristName) { this.touristName = touristName; }
    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getModerationStatus() { return moderationStatus; }
    public void setModerationStatus(String moderationStatus) {
        this.moderationStatus = moderationStatus == null || moderationStatus.isBlank() ? "VISIBLE" : moderationStatus;
    }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public List<String> getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(List<String> photoUrls) { this.photoUrls = photoUrls == null ? new ArrayList<>() : photoUrls; }
}
