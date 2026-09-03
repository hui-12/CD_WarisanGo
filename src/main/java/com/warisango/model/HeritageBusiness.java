package com.warisango.model;

import java.time.Instant;

public record HeritageBusiness(
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
        Instant rejectedAt
) {
}
