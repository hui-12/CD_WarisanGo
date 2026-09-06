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
import com.warisango.exception.FirebasePersistenceException;
import com.warisango.model.HeritageBusiness;
import com.warisango.model.GeoCoordinates;
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
        Map<String, Object> firestoreUpdates = new LinkedHashMap<>(corrections);
        Object location = firestoreUpdates.get("location");
        if (location instanceof GeoCoordinates coordinates) {
            firestoreUpdates.put("location", new GeoPoint(coordinates.latitude(), coordinates.longitude()));
        }
        document.getReference().update(firestoreUpdates).get();
    }

    public List<HeritageBusiness> findApprovedBusinesses() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<HeritageBusiness> list = new ArrayList<>();

        for (QueryDocumentSnapshot doc : documents) {
            if (!isApproved(doc)) {
                continue;
            }
            list.add(toHeritageBusiness(doc));
        }
        return list;
    }

    /**
     * Loads one approved business for Review pages.
     * The normal Firestore document ID is used first, with a businessId field query as a fallback.
     */
    public HeritageBusiness findByBusinessId(String businessId)
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

        return toHeritageBusiness(document);
    }

    // Real-time Firestore Snapshot Listener
    public Runnable addApprovedBusinessesListener(Consumer<List<HeritageBusiness>> callback) {
        ListenerRegistration registration = firestore.collection(COLLECTION_NAME)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) {
                        return;
                    }

                    List<HeritageBusiness> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        if (!isApproved(doc)) {
                            continue;
                        }
                        list.add(toHeritageBusiness(doc));
                    }
                    callback.accept(list);
        });
        return registration::remove;
    }

    // Find single business by document id
    public Optional<HeritageBusiness> findById(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME).document(id).get().get();
            if (doc == null || !doc.exists()) {
                return Optional.empty();
            }
            if (!isApproved(doc)) {
                return Optional.empty();
            }

            return Optional.of(toHeritageBusiness(doc));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new FirebasePersistenceException("Loading the heritage business was interrupted.", exception);
        } catch (Exception exception) {
            throw new FirebasePersistenceException("Unable to load the heritage business.", exception);
        }
    }

    private boolean isApproved(DocumentSnapshot document) {
        String status = document.getString("status");
        return status != null && "approved".equalsIgnoreCase(status.trim());
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

    public void updateBusiness(String businessId, HeritageBusiness business) {
        Map<String, Object> updates = new LinkedHashMap<>();
        updates.put("name", nullIfBlank(business.name()));
        updates.put("address", nullIfBlank(business.address()));
        updates.put("state", nullIfBlank(business.state()));
        updates.put("city", nullIfBlank(business.city()));
        updates.put("description", nullIfBlank(business.description()));
        updates.put("location", toGeoPoint(business.latitude(), business.longitude()));
        updates.put("operatingHour", nullIfBlank(business.operatingHour()));
        updates.put("averageRating", business.averageRating());
        updates.put("checkInPoints", business.checkInPoints() == null ? 50 : business.checkInPoints());
        updates.put("sourceVideoLink", nullIfBlank(business.sourceVideoLink()));
        executeUpdate(businessId, updates, "update the heritage business");
    }

    public void saveAll(List<HeritageBusiness> businesses) {
        try {
            WriteBatch batch = firestore.batch();
            for (HeritageBusiness business : businesses) {
                DocumentReference document = firestore.collection(COLLECTION_NAME).document();
                Map<String, Object> storedBusiness = toCreateDocument(business);
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

    private Map<String, Object> toCreateDocument(HeritageBusiness business) {
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("address", nullIfBlank(business.address()));
        document.put("averageRating", business.averageRating());
        document.put("checkInPoints", business.checkInPoints());
        document.put("city", nullIfBlank(business.city()));
        document.put("description", nullIfBlank(business.description()));
        document.put("location", toGeoPoint(business.latitude(), business.longitude()));
        document.put("name", nullIfBlank(business.name()));
        document.put("operatingHour", nullIfBlank(business.operatingHour()));
        document.put("sourceVideoLink", nullIfBlank(business.sourceVideoLink()));
        document.put("state", nullIfBlank(business.state()));
        document.put("status", nullIfBlank(business.status()));
        document.put("approveAt", null);
        document.put("rejectedAt", null);
        return document;
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
