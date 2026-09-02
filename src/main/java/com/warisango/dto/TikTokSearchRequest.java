package com.warisango.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TikTokSearchRequest(
        @NotBlank(message = "Search keyword cannot be empty.")
        @Size(max = 200, message = "Search keyword cannot exceed 200 characters.")
        String keyword,
        @NotNull(message = "Maximum videos is required.")
        @Min(value = 1, message = "Maximum videos must be at least 1.")
        @Max(value = 50, message = "Maximum videos cannot exceed 50.")
        Integer maximumVideos,
        @NotNull(message = "Scroll count is required.")
        @Min(value = 1, message = "Scroll count must be at least 1.")
        @Max(value = 20, message = "Scroll count cannot exceed 20.")
        Integer scrollCount,
        @NotNull(message = "Wait time is required.")
        @Min(value = 500, message = "Wait time must be at least 500 milliseconds.")
        @Max(value = 10000, message = "Wait time cannot exceed 10000 milliseconds.")
        Integer waitTime
) {
}
