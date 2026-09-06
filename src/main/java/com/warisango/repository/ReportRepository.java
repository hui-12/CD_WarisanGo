package com.warisango.repository;

import com.warisango.exception.FirebasePersistenceException;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.warisango.model.ReviewReport;
import com.warisango.util.ReviewDateFormatter;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firestore access for the root-level reviewReports collection.
 */
@Repository
public class ReportRepository {

    private static final String COLLECTION = "reviewReports";

    private final Firestore firestore;

    public ReportRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<ReviewReport> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION).get();
            List<ReviewReport> reports = new ArrayList<>();

            for (QueryDocumentSnapshot document : future.get().getDocuments()) {
                reports.add(convertDocumentToReport(document));
            }

            return reports;
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to load reports.", e);
        }
    }

    public ReviewReport findByReportId(String reportId) {
        try {
            DocumentSnapshot document = firestore.collection(COLLECTION)
                    .document(reportId)
                    .get()
                    .get();

            return document.exists() ? convertDocumentToReport(document) : null;
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to load report: " + reportId, e);
        }
    }

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
            throw new FirebasePersistenceException("Failed to check for duplicate report.", e);
        }
    }

    public void save(ReviewReport report) {
        if (report == null || report.reportId() == null || report.reportId().isBlank()) {
            throw new IllegalArgumentException("Report ID cannot be empty.");
        }

        try {
            firestore.collection(COLLECTION)
                    .document(report.reportId())
                    .set(toCreateDocument(report))
                    .get();
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to save report: " + report.reportId(), e);
        }
    }

    public void update(ReviewReport report) {
        if (report == null || report.reportId() == null || report.reportId().isBlank()) {
            throw new IllegalArgumentException("Report ID cannot be empty.");
        }

        try {
            firestore.collection(COLLECTION)
                    .document(report.reportId())
                    .update(toUpdateDocument(report))
                    .get();
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to update report: " + report.reportId(), e);
        }
    }

    public String generateNextReportId() {
        return firestore.collection(COLLECTION).document().getId();
    }

    private ReviewReport convertDocumentToReport(DocumentSnapshot document) {
        return new ReviewReport(
                getString(document, "reportId"), getString(document, "reporterTouristId"),
                getString(document, "targetType"), getString(document, "reviewId"),
                getString(document, "commentId"), getString(document, "reason"),
                getString(document, "status"), getTimestampText(document, "createdAt"),
                getString(document, "resolvedBy"), getTimestampText(document, "resolvedAt"));
    }

    private Map<String, Object> toCreateDocument(ReviewReport report) {
        Map<String, Object> data = new HashMap<>();
        data.put("reportId", report.reportId());
        data.put("reporterTouristId", report.reporterTouristId());
        data.put("targetType", report.targetType());
        data.put("reason", report.reason());
        data.put("status", report.status());
        data.put("createdAt", FieldValue.serverTimestamp());

        if (report.reviewId() != null && !report.reviewId().isBlank()) {
            data.put("reviewId", report.reviewId());
        }
        if (report.commentId() != null && !report.commentId().isBlank()) {
            data.put("commentId", report.commentId());
        }

        return data;
    }

    private Map<String, Object> toUpdateDocument(ReviewReport report) {
        Map<String, Object> data = new HashMap<>();
        data.put("status", report.status());
        data.put("resolvedBy", report.resolvedBy());
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
