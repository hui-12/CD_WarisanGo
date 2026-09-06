package com.warisango.model;

/** Represents a document in the Firestore comments collection. */
public record Comment(
        String commentId,
        String reviewId,
        String touristId,
        String commentText,
        String replyToCommentId,
        String replyToTouristName,
        String createdAt,
        String updatedAt,
        String moderationStatus) {
}
