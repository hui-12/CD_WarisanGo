package com.warisango.model;

import java.time.Instant;

public record ChallengeParticipation(
        String participationId,
        String touristId,
        String challengeId,
        String progress,
        String status,
        Instant completedDate) {
}
