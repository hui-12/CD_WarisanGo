package com.warisango.dto;

import jakarta.validation.constraints.NotBlank;

public record VideoProcessingRequest(
        @NotBlank(message = "Video URL cannot be empty.")
        String videoUrl
) {
}
