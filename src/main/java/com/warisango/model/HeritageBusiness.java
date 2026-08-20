package com.warisango.model;

import java.time.Instant;

public record HeritageBusiness(
        String businessId,
        String name,
        String address,
        String city,
        String description,
        Double latitude,
        Double longitude,
        String sourceVideoLink,
        String status,
        Double averageRating,
        Instant createdAt,
        Instant approveAt,
        Instant rejectAt
) {
}
