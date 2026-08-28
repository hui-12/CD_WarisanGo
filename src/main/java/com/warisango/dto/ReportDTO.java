package com.warisango.dto;

/**
 * Carries Report data between the moderation pages and service layer.
 */
public class ReportDTO {

    private String reportId;
    private String reporterTouristId;
    private String targetType;
    private String reviewId;
    private String commentId;
    private String reason;
    private String status;
    private String createdAt;
    private String resolvedBy;
    private String resolvedAt;

    /** Display-only date values; the original timestamp text remains available for sorting. */
    private String createdAtDisplay;
    private String resolvedAtDisplay;

    /** Display-only content loaded from Reviews or Comments. */
    private String targetText;

    /** Display-only owner ID loaded from Reviews or Comments. */
    private String targetOwnerId;

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getReporterTouristId() {
        return reporterTouristId;
    }

    public void setReporterTouristId(String reporterTouristId) {
        this.reporterTouristId = reporterTouristId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public String getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(String resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getCreatedAtDisplay() {
        return createdAtDisplay;
    }

    public void setCreatedAtDisplay(String createdAtDisplay) {
        this.createdAtDisplay = createdAtDisplay;
    }

    public String getResolvedAtDisplay() {
        return resolvedAtDisplay;
    }

    public void setResolvedAtDisplay(String resolvedAtDisplay) {
        this.resolvedAtDisplay = resolvedAtDisplay;
    }

    public String getTargetText() {
        return targetText;
    }

    public void setTargetText(String targetText) {
        this.targetText = targetText;
    }

    public String getTargetOwnerId() {
        return targetOwnerId;
    }

    public void setTargetOwnerId(String targetOwnerId) {
        this.targetOwnerId = targetOwnerId;
    }

    public String getTargetId() {
        return "COMMENT".equals(targetType) ? commentId : reviewId;
    }
}
