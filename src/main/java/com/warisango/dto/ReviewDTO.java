package com.warisango.dto;

import java.util.ArrayList;
import java.util.List;

public class ReviewDTO {

    private String reviewId;
    private String businessId;
    private String touristId;
    private String touristName;

    private String reviewText;

    private int rating;

    private List<String> photoUrls;

    private String createdAt;

    private String updatedAt;

    public ReviewDTO() {
        this.photoUrls = new ArrayList<>();
    }

    public ReviewDTO(String reviewId,
                     String businessId,
                     String touristId,
                     String touristName,
                     String reviewText,
                     int rating,
                     List<String> photoUrls,
                     String createdAt,
                     String updatedAt) {

        this.reviewId = reviewId;
        this.businessId = businessId;
        this.touristId = touristId;
        this.touristName = touristName;
        this.reviewText = reviewText;
        this.rating = rating;
        this.photoUrls = photoUrls;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getTouristId() {
        return touristId;
    }

    public void setTouristId(String touristId) {
        this.touristId = touristId;
    }

    public String getTouristName() {
        return touristName;
    }

    public void setTouristName(String touristName) {
        this.touristName = touristName;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public List<String> getPhotoUrls() {
        return photoUrls;
    }

    public void setPhotoUrls(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}