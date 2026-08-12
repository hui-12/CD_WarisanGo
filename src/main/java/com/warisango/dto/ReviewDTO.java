package com.warisango.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

/**
 * Carries review and rating data between the review pages and service layer.
 */
public class ReviewDTO {

    private String reviewId;

    @NotBlank(message = "Business is required.")
    private String businessId;

    private String touristId;

    @NotBlank(message = "Your name is required.")
    private String touristName;

    @NotBlank(message = "Review text is required.")
    private String reviewText;

    @Min(value = 1, message = "Please choose a rating.")
    @Max(value = 5, message = "Rating cannot be more than 5.")
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
