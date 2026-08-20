package com.warisango.dto;

import java.time.Instant;

public record HeritageBusinessView(
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
