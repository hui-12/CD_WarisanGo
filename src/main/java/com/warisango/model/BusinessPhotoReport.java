package com.warisango.model;

import java.time.Instant;

/** Represents an inappropriate-photo report submitted for admin review. */
public record BusinessPhotoReport(
        String reportId, String photoId, String businessId, String reporterId,
        String reason, String details, String status, Instant reportedAt,
        String resolvedBy, Instant resolvedAt) {
}
