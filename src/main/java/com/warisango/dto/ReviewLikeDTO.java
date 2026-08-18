package com.warisango.dto;

/**
 * Carries one ReviewLikes document.
 */
public class ReviewLikeDTO {

    private String likeId;
    private String reviewId;
    private String touristId;
    private String createdAt;

    public ReviewLikeDTO() {
    }

    public ReviewLikeDTO(String likeId, String reviewId, String touristId, String createdAt) {
        this.likeId = likeId;
        this.reviewId = reviewId;
        this.touristId = touristId;
        this.createdAt = createdAt;
    }

    public String getLikeId() {
        return likeId;
    }

    public void setLikeId(String likeId) {
        this.likeId = likeId;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getTouristId() {
        return touristId;
    }

    public void setTouristId(String touristId) {
        this.touristId = touristId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
