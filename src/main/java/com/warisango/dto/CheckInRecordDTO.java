package com.warisango.dto;

import java.time.Instant;

/**
 * Read projection used to build the recent check-in list.
 */
public record CheckInRecordDTO(
        String businessName,
        int pointsEarned,
        Instant timestamp) {
}
