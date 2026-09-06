package com.warisango.model;

import java.time.Instant;

public record TouristBadge(
        String touristBadgeId,
        String badgeId,
        String touristId,
        Instant dateEarned) {
}
