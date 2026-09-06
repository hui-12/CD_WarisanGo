package com.warisango.repository;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.warisango.exception.FirebasePersistenceException;
import com.warisango.model.BusinessPhotoReport;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BusinessPhotoReportRepository {
    private static final String COLLECTION = "businessPhotoReports";
    private final Firestore firestore;

    public BusinessPhotoReportRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public void save(String photoId, String businessId, String reporterId, String reason, String details) {
        try {
            var reference = firestore.collection(COLLECTION).document();
            Map<String, Object> data = new HashMap<>();
            data.put("reportId", reference.getId());
            data.put("photoId", photoId);
            data.put("businessId", businessId);
            data.put("reporterId", reporterId);
            data.put("reason", reason);
            data.put("details", details);
            data.put("status", "PENDING");
            data.put("reportedAt", FieldValue.serverTimestamp());
            data.put("resolvedBy", null);
            data.put("resolvedAt", null);
            reference.set(data).get();
        } catch (Exception exception) {
            throw new FirebasePersistenceException("Failed to report the business photo.", exception);
        }
    }

    public boolean hasPendingReport(String photoId, String reporterId) {
        try {
            return !firestore.collection(COLLECTION).whereEqualTo("photoId", photoId)
                    .get().get().getDocuments().stream()
                    .filter(document -> reporterId.equals(document.getString("reporterId")))
                    .filter(document -> "PENDING".equalsIgnoreCase(document.getString("status")))
                    .toList().isEmpty();
        } catch (Exception exception) {
            throw new FirebasePersistenceException("Failed to check the photo report.", exception);
        }
    }

    public List<BusinessPhotoReport> findAll() {
        try {
            return firestore.collection(COLLECTION).get().get().getDocuments().stream().map(this::toModel).toList();
        } catch (Exception exception) {
            throw new FirebasePersistenceException("Failed to load business photo reports.", exception);
        }
    }

    public BusinessPhotoReport findById(String reportId) {
        try {
            DocumentSnapshot document = firestore.collection(COLLECTION).document(reportId).get().get();
            return document.exists() ? toModel(document) : null;
        } catch (Exception exception) {
            throw new FirebasePersistenceException("Failed to load the photo report.", exception);
        }
    }

    public void resolve(String reportId, String status, String adminId) {
        try {
            firestore.collection(COLLECTION).document(reportId).update(Map.of(
                    "status", status, "resolvedBy", adminId, "resolvedAt", FieldValue.serverTimestamp())).get();
        } catch (Exception exception) {
            throw new FirebasePersistenceException("Failed to resolve the photo report.", exception);
        }
    }

    private BusinessPhotoReport toModel(DocumentSnapshot document) {
        return new BusinessPhotoReport(
                value(document, "reportId", document.getId()), document.getString("photoId"),
                document.getString("businessId"), document.getString("reporterId"),
                document.getString("reason"), document.getString("details"),
                value(document, "status", "PENDING"), toInstant(document, "reportedAt"),
                document.getString("resolvedBy"), toInstant(document, "resolvedAt"));
    }

    private String value(DocumentSnapshot document, String field, String fallback) {
        String value = document.getString(field);
        return value == null || value.isBlank() ? fallback : value;
    }

    private Instant toInstant(DocumentSnapshot document, String field) {
        var timestamp = document.getTimestamp(field);
        return timestamp == null ? null : timestamp.toDate().toInstant();
    }
}
