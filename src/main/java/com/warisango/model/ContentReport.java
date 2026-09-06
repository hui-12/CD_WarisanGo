package com.warisango.model;

/** Represents a document in the Firestore reports collection. */
public record ContentReport(
        String reportId,
        String reporterTouristId,
        String targetType,
        String reviewId,
        String commentId,
        String reason,
        String status,
        String createdAt,
        String resolvedBy,
        String resolvedAt) {
}
