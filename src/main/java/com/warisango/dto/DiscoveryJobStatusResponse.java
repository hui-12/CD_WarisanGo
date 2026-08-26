package com.warisango.dto;

public record DiscoveryJobStatusResponse(
        String jobId,
        String status,
        int progress,
        String message,
        DiscoveryProcessResponse result
) {
}
