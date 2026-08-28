package com.warisango.dto;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record VisitDTO(
        String checkInId,
        String businessId,
        String businessName,
        String image,
        Instant visitedAt,
        int points
) {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter
            .ofPattern("d MMM uuuu, h:mm a")
            .withZone(ZoneId.of("Asia/Kuala_Lumpur"));

    public String formattedVisitedAt() {
        return DISPLAY_FORMAT.format(visitedAt);
    }
}
