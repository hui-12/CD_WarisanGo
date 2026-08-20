package com.warisango.dto;

import jakarta.validation.constraints.NotBlank;

public record AIExtractionRequest(
        @NotBlank(message = "Transcript cannot be empty.")
        String transcript
) {
}
