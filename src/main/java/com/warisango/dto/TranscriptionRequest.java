package com.warisango.dto;

import jakarta.validation.constraints.NotBlank;

public record TranscriptionRequest(
        @NotBlank(message = "Audio file path cannot be empty.")
        String audioFile
) {
}
