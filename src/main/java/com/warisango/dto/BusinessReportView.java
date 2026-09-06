package com.warisango.dto;

import com.google.cloud.Timestamp;
import com.warisango.model.BusinessReport;
import com.warisango.util.ReviewDateFormatter;

/**
 * Admin-facing report view enriched with tourist and business information.
 */
public record BusinessReportView(
        String reportId,
        String touristId,
        String businessId,
        String reason,
        String details,
        String status,
        Timestamp submittedAt,
        String submittedAtDisplay,
        String resolvedBy,
        Timestamp resolvedAt,
        String resolutionNote,
        String touristName,
        String touristAvatar,
        HeritageBusinessDTO reportedBusiness) {

    public static BusinessReportView from(
            BusinessReport report,
            String touristName,
            String touristAvatar,
            HeritageBusinessDTO reportedBusiness) {
        return new BusinessReportView(
                report.getReportId(),
                report.getTouristId(),
                report.getBusinessId(),
                report.getReason(),
                report.getDetails(),
                report.getStatus(),
                report.getSubmittedAt(),
                ReviewDateFormatter.format(report.getSubmittedAt()),
                report.getResolvedBy(),
                report.getResolvedAt(),
                report.getResolutionNote(),
                touristName,
                touristAvatar,
                reportedBusiness);
    }
}
