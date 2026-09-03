package com.warisango.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.ListenerRegistration;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
import com.warisango.dto.HeritageBusinessUpdateRequest;
import com.warisango.dto.HeritageBusinessDTO;
import com.warisango.exception.FirebasePersistenceException;
import com.warisango.model.HeritageBusiness;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@Repository
public class BusinessRepository {

    private static final String COLLECTION_NAME = "heritageBusinesses";
    private final Firestore firestore;

    public BusinessRepository(Firestore firestore) {
        this.firestore = firestore;
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
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new FirebasePersistenceException("Loading the heritage business was interrupted.", exception);
        } catch (Exception exception) {
            throw new FirebasePersistenceException("Unable to load the heritage business.", exception);
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

    public List<HeritageBusiness> findByStatus(String status) {
        return findByStatuses(List.of(status));
    }

    public List<HeritageBusiness> findByStatuses(List<String> statuses) {
        try {
            return firestore.collection(COLLECTION_NAME)
                    .whereIn("status", statuses)
                    .get()
                    .get()
                    .getDocuments()
                    .stream()
                    .map(this::toHeritageBusiness)
                    .toList();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new FirebasePersistenceException("Loading heritage businesses was interrupted.", exception);
        } catch (Exception exception) {
            throw new FirebasePersistenceException("Unable to load heritage businesses.", exception);
        }
    }

    public Optional<HeritageBusiness> findHeritageBusinessById(String businessId) {
        try {
            DocumentSnapshot document = firestore.collection(COLLECTION_NAME)
                    .document(businessId)
                    .get()
                    .get();
            return document.exists() ? Optional.of(toHeritageBusiness(document)) : Optional.empty();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new FirebasePersistenceException("Loading the heritage business was interrupted.", exception);
        } catch (Exception exception) {
            throw new FirebasePersistenceException("Unable to load the heritage business.", exception);
        }
    }

    public void approve(String businessId) {
        updateReviewStatus(businessId, "Approved", "approveAt");
    }

    public void reject(String businessId) {
        updateReviewStatus(businessId, "Rejected", "rejectedAt");
    }

    public void updateBusiness(String businessId, HeritageBusinessUpdateRequest request) {
        Map<String, Object> updates = new LinkedHashMap<>();
        updates.put("name", nullIfBlank(request.getName()));
        updates.put("address", nullIfBlank(request.getAddress()));
        updates.put("state", nullIfBlank(request.getState()));
        updates.put("city", nullIfBlank(request.getCity()));
        updates.put("description", nullIfBlank(request.getDescription()));
        updates.put("location", toGeoPoint(request.getLatitude(), request.getLongitude()));
        updates.put("operatingHour", nullIfBlank(request.getOperatingHour()));
        updates.put("averageRating", request.getAverageRating());
        updates.put("checkInPoints", request.getCheckInPoints() == null ? 50 : request.getCheckInPoints());
        updates.put("sourceVideoLink", nullIfBlank(request.getSourceVideoLink()));
        executeUpdate(businessId, updates, "update the heritage business");
    }

    public void saveAll(List<Map<String, Object>> businesses) {
        try {
            WriteBatch batch = firestore.batch();
            for (Map<String, Object> business : businesses) {
                DocumentReference document = firestore.collection(COLLECTION_NAME).document();
                Map<String, Object> storedBusiness = new LinkedHashMap<>(business);
                String businessId = document.getId();
                storedBusiness.put("businessId", businessId);
                storedBusiness.put("createdAt", FieldValue.serverTimestamp());
                batch.set(document, storedBusiness);
            }
            batch.commit().get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new FirebasePersistenceException("Saving heritage businesses was interrupted.", exception);
        } catch (Exception exception) {
            throw new FirebasePersistenceException("Unable to save heritage businesses.", exception);
        }
    }

    private void updateReviewStatus(String businessId, String status, String timestampField) {
        Map<String, Object> updates = new LinkedHashMap<>();
        updates.put("status", status);
        updates.put(timestampField, FieldValue.serverTimestamp());
        executeUpdate(businessId, updates, "update the heritage business status");
    }

    private void executeUpdate(String businessId, Map<String, Object> updates, String action) {
        try {
            firestore.collection(COLLECTION_NAME).document(businessId).update(updates).get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new FirebasePersistenceException("Attempting to " + action + " was interrupted.", exception);
        } catch (Exception exception) {
            throw new FirebasePersistenceException("Unable to " + action + ".", exception);
        }
    }

    private HeritageBusiness toHeritageBusiness(DocumentSnapshot document) {
        GeoPoint location = document.getGeoPoint("location");
        return new HeritageBusiness(
                document.getId(),
                document.getString("name"),
                document.getString("address"),
                document.getString("state"),
                document.getString("city"),
                document.getString("description"),
                location == null ? null : location.getLatitude(),
                location == null ? null : location.getLongitude(),
                document.getString("operatingHour"),
                document.getString("sourceVideoLink"),
                document.getString("status"),
                document.getDouble("averageRating"),
                document.getLong("checkInPoints") == null ? null : document.getLong("checkInPoints").intValue(),
                toInstant(document, "createdAt"),
                toInstant(document, "approveAt"),
                toInstant(document, "rejectedAt"));
    }

    private Instant toInstant(DocumentSnapshot document, String field) {
        com.google.cloud.Timestamp timestamp = document.getTimestamp(field);
        return timestamp == null ? null : Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }

    private GeoPoint toGeoPoint(Double latitude, Double longitude) {
        return latitude == null || longitude == null ? null : new GeoPoint(latitude, longitude);
    }

    private String nullIfBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
