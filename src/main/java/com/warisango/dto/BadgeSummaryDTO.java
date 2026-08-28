package com.warisango.dto;

public record BadgeSummaryDTO(
        int total,
        int earned,
        int locked,
        BadgeDTO featuredBadge
) {
}
