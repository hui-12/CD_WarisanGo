package com.warisango.model.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.warisango.dto.ReportDTO;
import com.warisango.util.ReviewDateFormatter;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firestore access for the root-level reports collection.
 */
@Repository
public class ReportRepositoryImpl implements ReportRepository {

    private static final String COLLECTION = "reports";

    private final Firestore firestore;

    public ReportRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public List<ReportDTO> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION).get();
            List<ReportDTO> reports = new ArrayList<>();

            for (QueryDocumentSnapshot document : future.get().getDocuments()) {
                reports.add(convertDocumentToReport(document));
            }

            return reports;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load reports.", e);
        }
    }

    @Override
    public ReportDTO findByReportId(String reportId) {
        try {
            DocumentSnapshot document = firestore.collection(COLLECTION)
                    .document(reportId)
                    .get()
                    .get();

            return document.exists() ? convertDocumentToReport(document) : null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load report: " + reportId, e);
        }
    }

    @Override
    public boolean existsByReporterAndTarget(
            String reporterUserId,
            String targetType,
            String targetId) {
        String targetField = "COMMENT".equals(targetType) ? "commentId" : "reviewId";

        try {
            return !firestore.collection(COLLECTION)
                    .whereEqualTo("reporterTouristId", reporterUserId)
                    .whereEqualTo("targetType", targetType)
                    .whereEqualTo(targetField, targetId)
                    .limit(1)
                    .get()
                    .get()
                    .isEmpty();
        } catch (Exception e) {
            throw new RuntimeException("Failed to check for duplicate report.", e);
        }
    }

    @Override
    public void save(ReportDTO report) {
        if (report == null || report.getReportId() == null || report.getReportId().isBlank()) {
            throw new IllegalArgumentException("Report ID cannot be empty.");
        }

        try {
            firestore.collection(COLLECTION)
                    .document(report.getReportId())
                    .set(toCreateDocument(report))
                    .get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save report: " + report.getReportId(), e);
        }
    }

    @Override
    public void update(ReportDTO report) {
        if (report == null || report.getReportId() == null || report.getReportId().isBlank()) {
            throw new IllegalArgumentException("Report ID cannot be empty.");
        }

        try {
            firestore.collection(COLLECTION)
                    .document(report.getReportId())
                    .update(toUpdateDocument(report))
                    .get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update report: " + report.getReportId(), e);
        }
    }

    @Override
    public String generateNextReportId() {
        try {
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION)
                    .get()
                    .get()
                    .getDocuments();

            int maxNumber = 0;
            for (QueryDocumentSnapshot document : documents) {
                String reportId = document.getString("reportId");
                if (reportId == null || !reportId.startsWith("report_")) {
                    continue;
                }

                try {
                    maxNumber = Math.max(
                            maxNumber,
                            Integer.parseInt(reportId.substring("report_".length()))
                    );
                } catch (NumberFormatException ignored) {
                    // Ignore IDs outside the report_001 format.
                }
            }

            return String.format("report_%03d", maxNumber + 1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate next report ID.", e);
        }
    }

    private ReportDTO convertDocumentToReport(DocumentSnapshot document) {
        ReportDTO report = new ReportDTO();
        report.setReportId(getString(document, "reportId"));
        report.setReporterTouristId(getString(document, "reporterTouristId"));
        report.setTargetType(getString(document, "targetType"));
        report.setReviewId(getString(document, "reviewId"));
        report.setCommentId(getString(document, "commentId"));
        report.setReason(getString(document, "reason"));
        report.setStatus(getString(document, "status"));
        String createdAt = getTimestampText(document, "createdAt");
        String resolvedAt = getTimestampText(document, "resolvedAt");
        report.setCreatedAt(createdAt);
        report.setCreatedAtDisplay(ReviewDateFormatter.format(createdAt));
        report.setResolvedBy(getString(document, "resolvedBy"));
        report.setResolvedAt(resolvedAt);
        report.setResolvedAtDisplay(ReviewDateFormatter.format(resolvedAt));
        return report;
    }

    private Map<String, Object> toCreateDocument(ReportDTO report) {
        Map<String, Object> data = new HashMap<>();
        data.put("reportId", report.getReportId());
        data.put("reporterTouristId", report.getReporterTouristId());
        data.put("targetType", report.getTargetType());
        data.put("reason", report.getReason());
        data.put("status", report.getStatus());
        data.put("createdAt", FieldValue.serverTimestamp());

        if (report.getReviewId() != null && !report.getReviewId().isBlank()) {
            data.put("reviewId", report.getReviewId());
        }
        if (report.getCommentId() != null && !report.getCommentId().isBlank()) {
            data.put("commentId", report.getCommentId());
        }

        return data;
    }

    private Map<String, Object> toUpdateDocument(ReportDTO report) {
        Map<String, Object> data = new HashMap<>();
        data.put("status", report.getStatus());
        data.put("resolvedBy", report.getResolvedBy());
        data.put("resolvedAt", FieldValue.serverTimestamp());
        return data;
    }

    private String getString(DocumentSnapshot document, String field) {
        String value = document.getString(field);
        return value == null ? "" : value;
    }

    private String getTimestampText(DocumentSnapshot document, String field) {
        Object value = document.get(field);
        return value == null ? "" : value.toString();
    }
}
