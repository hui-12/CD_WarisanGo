package com.warisango.dto;

import java.time.Instant;

public record ProcessedBusinessEntryView(
        String businessId,
        String businessName,
        String address,
        String status,
        Instant reviewedAt,
        String reviewedAtDisplay,
        String photoId,
        String photoUrl,
        String photoUploaderName,
        String photoUploadedAtDisplay,
        int photoCount
) {
}
