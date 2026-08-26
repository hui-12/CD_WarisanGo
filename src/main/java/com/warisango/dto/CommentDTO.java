package com.warisango.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Carries comment data between the review detail page and the Comments collection.
 */
public class CommentDTO {

    private String commentId;

    @NotBlank(message = "Review is required.")
    private String reviewId;

    private String touristId;

    private String touristName;

    @NotBlank(message = "Comment cannot be empty.")
    @Size(max = 500, message = "Comment cannot be longer than 500 characters.")
    private String commentText;

    /** Optional flat reply reference. Replies are displayed in the same comment feed. */
    private String replyToCommentId;

    /** Display-only name of the comment author being replied to. */
    private String replyToTouristName;

    private String createdAt;

    private String updatedAt;

    /**
     * Display-only like state loaded from the root-level CommentLikes collection.
     */
    private int likeCount;

    private boolean likedByCurrentUser;

    /**
     * Content visibility state used by the moderation workflow.
     * Existing documents without this field are treated as VISIBLE.
     */
    private String moderationStatus = "VISIBLE";

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
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

    public String getTouristName() {
        return touristName;
    }

    public void setTouristName(String touristName) {
        this.touristName = touristName;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getReplyToCommentId() {
        return replyToCommentId;
    }

    public void setReplyToCommentId(String replyToCommentId) {
        this.replyToCommentId = replyToCommentId;
    }

    public String getReplyToTouristName() {
        return replyToTouristName;
    }

    public void setReplyToTouristName(String replyToTouristName) {
        this.replyToTouristName = replyToTouristName;
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

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public void setLikedByCurrentUser(boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }

    public String getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(String moderationStatus) {
        this.moderationStatus = moderationStatus == null || moderationStatus.isBlank()
                ? "VISIBLE"
                : moderationStatus;
    }
}
