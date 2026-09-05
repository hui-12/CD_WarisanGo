package com.warisango.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record ChallengeDefinitionRequest(
        @NotBlank @Size(max = 80) String title,
        @NotBlank @Size(max = 240) String description,
        @NotBlank @Size(max = 120) String requirement,
        @NotBlank @Size(max = 80) String badge,
        @NotBlank String expiry,
        @Min(1) @Max(300) int target,
        @Min(1) @Max(10000) int rewardPoints,
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
