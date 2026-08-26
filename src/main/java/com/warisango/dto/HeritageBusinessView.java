package com.warisango.dto;

import java.time.Instant;

public record HeritageBusinessView(
        String businessId,
        String name,
        String address,
        String state,
        String city,
        String description,
        Double latitude,
        Double longitude,
        String operatingHour,
        String sourceVideoLink,
        String status,
        Double averageRating,
        Integer checkInPoints,
        Instant createdAt,
        Instant approveAt,
        Instant rejectAt
) {
}
