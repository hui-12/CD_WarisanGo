package com.warisango.model.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.ListenerRegistration;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.warisango.dto.HeritageBusinessDTO;
import com.warisango.model.Business;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@Repository
public class BusinessRepository {

    private static final String COLLECTION_NAME = "heritageBusinesses";
    private static final String LEGACY_COLLECTION_NAME = "HeritageBusinesses";
    private static final Logger logger = LoggerFactory.getLogger(BusinessRepository.class);
    private final Firestore firestore;

    public BusinessRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public int migrateLegacyCollection() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> legacyDocuments = firestore.collection(LEGACY_COLLECTION_NAME)
                .get()
                .get()
                .getDocuments();

        for (QueryDocumentSnapshot document : legacyDocuments) {
            Map<String, Object> data = new HashMap<>(document.getData());
            String storedId = document.getString("businessId");
            String businessId = storedId == null || storedId.isBlank() ? document.getId() : storedId.trim();

            data.put("businessId", businessId);
            data.putIfAbsent("averageRating", null);
            data.putIfAbsent("checkInPoints", 50L);
            data.remove("category");
            data.remove("story");
            data.remove("photos");
            data.putIfAbsent("operatingHour", "");
            data.putIfAbsent("approveAt", null);
            data.putIfAbsent("rejectedAt", null);
            data.putIfAbsent("sourceVideoLink", "");
            data.putIfAbsent("createdAt", com.google.cloud.Timestamp.now());

            String status = data.get("status") instanceof String value ? value : "Pending";
            data.put("status", "approved".equalsIgnoreCase(status) ? "Approved" : status);

            firestore.collection(COLLECTION_NAME)
                    .document(businessId)
                    .set(data, SetOptions.merge())
                    .get();
        }

        for (QueryDocumentSnapshot document : firestore.collection(COLLECTION_NAME).get().get().getDocuments()) {
            Map<String, Object> schemaUpdate = new HashMap<>();
            schemaUpdate.put("category", FieldValue.delete());
            schemaUpdate.put("story", FieldValue.delete());
            if (!document.contains("operatingHour")) {
                schemaUpdate.put("operatingHour", "");
            }
            if (!document.contains("approveAt")) {
                schemaUpdate.put("approveAt", null);
            }
            if (!document.contains("rejectedAt")) {
                schemaUpdate.put("rejectedAt", null);
            }
            document.getReference().update(schemaUpdate).get();
        }

        return legacyDocuments.size();
    }

    public boolean saveIfAbsent(Business business) throws ExecutionException, InterruptedException {
        String businessId = business.getBusinessId();
        var reference = businessId == null || businessId.isBlank()
                ? firestore.collection(COLLECTION_NAME).document()
                : firestore.collection(COLLECTION_NAME).document(businessId);
        if (businessId == null || businessId.isBlank()) {
            business.setBusinessId(reference.getId());
        }
        DocumentSnapshot existing = reference.get().get();
        if (existing.exists()) {
            Map<String, Object> schemaUpdate = new HashMap<>();
            schemaUpdate.put("category", FieldValue.delete());
            schemaUpdate.put("story", FieldValue.delete());
            if (!existing.contains("operatingHour")) {
                schemaUpdate.put("operatingHour", business.getOperatingHour());
            }
            if (!existing.contains("approveAt")) {
                schemaUpdate.put("approveAt", business.getApproveAt());
            }
            if (!existing.contains("rejectedAt")) {
                schemaUpdate.put("rejectedAt", business.getRejectedAt());
            }
            reference.update(schemaUpdate).get();
            return false;
        }

        reference.set(business).get();
        return true;
    }

    public void updateAverageRating(String businessId, Double averageRating)
            throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME)
                .document(businessId)
                .get()
                .get();

        if (!document.exists()) {
            QuerySnapshot matches = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("businessId", businessId)
                    .limit(1)
                    .get()
                    .get();
            if (matches.isEmpty()) {
                return;
            }
            document = matches.getDocuments().get(0);
        }

        Map<String, Object> update = new HashMap<>();
        update.put("averageRating", averageRating);
        document.getReference().update(update).get();
    }

    public void updateReportedDetails(String businessId, Map<String, Object> corrections)
            throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(businessId).get().get();
        if (!document.exists()) {
            QuerySnapshot matches = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("businessId", businessId)
                    .limit(1)
                    .get()
                    .get();
            if (matches.isEmpty()) {
                throw new IllegalArgumentException("Business was not found.");
            }
            document = matches.getDocuments().get(0);
        }
        document.getReference().update(corrections).get();
    }

    public List<HeritageBusinessDTO> findApprovedBusinesses() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<HeritageBusinessDTO> list = new ArrayList<>();

        for (QueryDocumentSnapshot doc : documents) {
            if (!isApproved(doc)) {
                continue;
            }
            GeoPoint geoPoint = doc.getGeoPoint("location");
            double lat = geoPoint != null ? geoPoint.getLatitude() : 0.0;
            double lng = geoPoint != null ? geoPoint.getLongitude() : 0.0;

            HeritageBusinessDTO dto = toDto(doc, lat, lng);
            list.add(dto);
        }
        return list;
    }

    /**
     * Loads one approved business for Review pages.
     * The normal Firestore document ID is used first, with a businessId field query as a fallback.
     */
    public HeritageBusinessDTO findByBusinessId(String businessId)
            throws ExecutionException, InterruptedException {
        if (businessId == null || businessId.isBlank()) {
            return null;
        }

        DocumentSnapshot document = firestore.collection(COLLECTION_NAME)
                .document(businessId)
                .get()
                .get();

        if (!document.exists()) {
            QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("businessId", businessId)
                    .limit(1)
                    .get()
                    .get();

            if (snapshot.isEmpty()) {
                return null;
            }

            document = snapshot.getDocuments().get(0);
        }

        if (!isApproved(document)) {
            return null;
        }

        return toBusinessDTO(document);
    }

    // Real-time Firestore Snapshot Listener
    public ListenerRegistration addApprovedBusinessesListener(Consumer<List<HeritageBusinessDTO>> callback) {
        return firestore.collection(COLLECTION_NAME)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) {
                        return;
                    }

                    List<HeritageBusinessDTO> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        if (!isApproved(doc)) {
                            continue;
                        }
                        GeoPoint geoPoint = doc.getGeoPoint("location");
                        double lat = geoPoint != null ? geoPoint.getLatitude() : 0.0;
                        double lng = geoPoint != null ? geoPoint.getLongitude() : 0.0;

                        HeritageBusinessDTO dto = toDto(doc, lat, lng);
                        list.add(dto);
                    }
                    callback.accept(list);
        });
    }

    // Find single business by document id
    public Optional<HeritageBusinessDTO> findById(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME).document(id).get().get();
            if (doc == null || !doc.exists()) {
                return Optional.empty();
            }
            if (!isApproved(doc)) {
                return Optional.empty();
            }

            GeoPoint geoPoint = doc.getGeoPoint("location");
            double lat = geoPoint != null ? geoPoint.getLatitude() : 0.0;
            double lng = geoPoint != null ? geoPoint.getLongitude() : 0.0;

            HeritageBusinessDTO dto = toDto(doc, lat, lng);

            return Optional.of(dto);
        } catch (Exception e) {
            logger.error("Error fetching business by id: {}", id, e);
            return Optional.empty();
        }
    }

    private HeritageBusinessDTO toDto(DocumentSnapshot doc, double latitude, double longitude) {
        HeritageBusinessDTO dto = new HeritageBusinessDTO(
                doc.getId(),
                doc.getString("name"),
                doc.getString("address"),
                doc.getString("state"),
                doc.getString("city"),
                doc.getString("description"),
                latitude,
                longitude,
                doc.getDouble("averageRating"),
                getCheckInPoints(doc)
        );
        dto.setSourceVideoLink(doc.getString("sourceVideoLink"));
        dto.setOperatingHour(doc.getString("operatingHour"));
        if (doc.getTimestamp("createdAt") != null) {
            dto.setCreatedAt(doc.getTimestamp("createdAt").toDate().toInstant());
        }
        if (doc.getTimestamp("approveAt") != null) {
            dto.setApproveAt(doc.getTimestamp("approveAt").toDate().toInstant());
        }
        if (doc.getTimestamp("rejectedAt") != null) {
            dto.setRejectedAt(doc.getTimestamp("rejectedAt").toDate().toInstant());
        }

        return dto;
    }

    private boolean isApproved(DocumentSnapshot document) {
        String status = document.getString("status");
        return status != null && "approved".equalsIgnoreCase(status.trim());
    }

    private HeritageBusinessDTO toBusinessDTO(DocumentSnapshot document) {
        GeoPoint geoPoint = document.getGeoPoint("location");
        double lat = geoPoint != null ? geoPoint.getLatitude() : 0.0;
        double lng = geoPoint != null ? geoPoint.getLongitude() : 0.0;
        return toDto(document, lat, lng);
    }

    private int getCheckInPoints(DocumentSnapshot doc) {
        Long points = doc.getLong("checkInPoints");
        return points != null && points > 0 ? points.intValue() : 50;
    }
}
