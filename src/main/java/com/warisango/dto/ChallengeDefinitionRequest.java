package com.warisango.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record ChallengeDefinitionRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String requirement,
        @NotBlank String badge,
        @NotBlank String expiry,
        @Min(1) int target,
        @Min(1) int rewardPoints,
        @NotBlank String status) {

    public Map<String, Object> toMap() {
        return Map.of(
                "title", title,
                "description", description,
                "requirement", requirement,
                "badge", badge,
                "expiry", expiry,
                "target", target,
                "rewardPoints", rewardPoints,
                "status", status
        );
    }
}
