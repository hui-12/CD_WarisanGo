package com.warisango.dto;

/**
 * Carries a ReviewPhotos document between the review module layers.
 */
public class ReviewPhotoDTO {

    private String photoId;
    private String reviewId;
    private String photoUrl;

    public ReviewPhotoDTO() {
    }

    public ReviewPhotoDTO(String photoId, String reviewId, String photoUrl) {
        this.photoId = photoId;
        this.reviewId = reviewId;
        this.photoUrl = photoUrl;
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
