package com.warisango.dto;

public record BusinessPhotoReportView(
        String reportId, String photoId, String businessId, String businessName,
        String imageUrl, String reporterId, String reporterName, String uploaderName,
        String reason, String details, String status, String reportedAtDisplay,
        boolean photoAvailable) {
}
