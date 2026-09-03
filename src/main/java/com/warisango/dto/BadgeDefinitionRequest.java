package com.warisango.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record BadgeDefinitionRequest(
        @NotBlank String name,
        @NotBlank String emoji,
        @NotBlank String description,
        @NotBlank String unlockCriteria,
        @NotBlank String criteriaType,
        @Min(1) int target) {

    public Map<String, Object> toMap() {
        return Map.of(
                "name", name,
                "emoji", emoji,
                "description", description,
                "unlockCriteria", unlockCriteria,
                "criteriaType", criteriaType,
                "target", target
        );
    }
}
