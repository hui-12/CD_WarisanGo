package com.warisango.dto;

import java.time.Instant;

public record BadgeDTO(
        String badgeId,
        String name,
        String emoji,
        String description,
        String unlockCriteria,
        String criteriaType,
        boolean earned,
        Instant dateEarned,
        int progress,
        int target
) {
}
