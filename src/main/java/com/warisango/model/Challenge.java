package com.warisango.model;

import java.time.Instant;

public record Challenge(
        String challengeId,
        String title,
        String description,
        String requirement,
        int target,
        int rewardPoints,
        String badge,
        String expiry,
        String status,
        Instant createdDate) {
}
