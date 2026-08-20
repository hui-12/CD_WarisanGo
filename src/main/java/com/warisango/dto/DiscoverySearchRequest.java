package com.warisango.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DiscoverySearchRequest(
        @NotBlank(message = "Search keyword cannot be empty.")
        @Size(max = 200, message = "Search keyword cannot exceed 200 characters.")
        String keyword
) {
}
