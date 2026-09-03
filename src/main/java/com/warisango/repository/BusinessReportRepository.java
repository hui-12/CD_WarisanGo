package com.warisango.repository;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.warisango.model.BusinessReport;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class BusinessReportRepository {
    private static final String COLLECTION = "businessReports";
    private final Firestore firestore;

    public BusinessReportRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public BusinessReport save(String touristId, String businessId, String reason, String details) throws Exception {
        var reference = firestore.collection(COLLECTION).document();
        Map<String, Object> data = new HashMap<>();
        data.put("reportId", reference.getId());
        data.put("touristId", touristId);
        data.put("businessId", businessId);
        data.put("reason", reason);
        data.put("details", details);
        data.put("status", "PENDING_REVIEW");
        data.put("submittedAt", FieldValue.serverTimestamp());
        data.put("resolvedBy", null);
        data.put("resolvedAt", null);
        data.put("resolutionNote", null);
        reference.set(data).get();
        return findById(reference.getId());
    }

    public List<BusinessReport> findAll() throws Exception {
        return firestore.collection(COLLECTION).get().get().getDocuments().stream()
                .map(document -> document.toObject(BusinessReport.class))
                .filter(Objects::nonNull)
                .toList();
    }

    public BusinessReport findById(String reportId) throws Exception {
        DocumentSnapshot document = firestore.collection(COLLECTION).document(reportId).get().get();
        return document.exists() ? document.toObject(BusinessReport.class) : null;
    }

    public void updateOutcome(String reportId, String status, String adminId, String note) throws Exception {
        Map<String, Object> update = new HashMap<>();
        update.put("status", status);
        update.put("resolvedBy", adminId);
        update.put("resolvedAt", FieldValue.serverTimestamp());
        update.put("resolutionNote", note);
        firestore.collection(COLLECTION).document(reportId).update(update).get();
    }
}
