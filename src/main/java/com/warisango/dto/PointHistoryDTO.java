package com.warisango.dto;

import java.time.Instant;

public record PointHistoryDTO(
        String transactionId,
        String type,
        String description,
        int points,
        Instant timestamp,
        String relatedChallengeId
) {
}
