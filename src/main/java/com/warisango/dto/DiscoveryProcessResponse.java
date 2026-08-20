package com.warisango.dto;

public record DiscoveryProcessResponse(
        String transcript,
        AIExtractionResult extraction,
        String recordId
) {
}
