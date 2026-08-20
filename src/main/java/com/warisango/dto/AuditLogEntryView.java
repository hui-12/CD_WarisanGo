package com.warisango.dto;

import java.time.Instant;

public record AuditLogEntryView(
        String businessId,
        String businessName,
        String address,
        String status,
        Instant reviewedAt,
        String reviewedAtDisplay
) {
}
