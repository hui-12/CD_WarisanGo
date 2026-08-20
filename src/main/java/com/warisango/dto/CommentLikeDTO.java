package com.warisango.dto;

/**
 * Carries one CommentLikes document.
 */
public class CommentLikeDTO {

    private String likeId;
    private String commentId;
    private String touristId;
    private String createdAt;

    public CommentLikeDTO() {
    }

    public CommentLikeDTO(String likeId, String commentId, String touristId, String createdAt) {
        this.likeId = likeId;
        this.commentId = commentId;
        this.touristId = touristId;
        this.createdAt = createdAt;
    }

    public String getLikeId() {
        return likeId;
    }

    public void setLikeId(String likeId) {
        this.likeId = likeId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
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
