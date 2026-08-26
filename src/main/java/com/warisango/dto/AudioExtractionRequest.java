package com.warisango.dto;

import jakarta.validation.constraints.NotBlank;

public record AudioExtractionRequest(
        @NotBlank(message = "Video URL cannot be empty.")
        String videoUrl
) {
}
