package com.warisango.model;

public record Badge(
        String badgeId,
        String name,
        String emoji,
        String description,
        String unlockCriteria,
        String criteriaType,
        int target) {
}
