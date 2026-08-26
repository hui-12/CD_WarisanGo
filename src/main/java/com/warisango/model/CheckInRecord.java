package com.warisango.model;

import java.time.Instant;

public record CheckInRecord(
        String businessName,
        int pointsEarned,
        Instant timestamp) {
}
