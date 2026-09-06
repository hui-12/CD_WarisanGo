package com.warisango.model;

import java.time.Instant;

public record PointsHistory(
        String transactionId,
        String activityType,
        int pointsEarned,
        Instant activityDate,
        String relatedChallengeId,
        String touristId
) {
}
