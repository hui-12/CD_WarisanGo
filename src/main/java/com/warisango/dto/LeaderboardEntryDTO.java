package com.warisango.dto;

public record LeaderboardEntryDTO(
        int rank,
        String userId,
        String name,
        int points,
        String tier
) {
    public LeaderboardEntryDTO withRank(int assignedRank) {
        return new LeaderboardEntryDTO(assignedRank, userId, name, points, tier);
    }
}
