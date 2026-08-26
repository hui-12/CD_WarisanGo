package com.warisango.model.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.WriteBatch;
import com.google.firebase.cloud.FirestoreClient;
import com.warisango.exception.FirebasePersistenceException;
import com.warisango.model.HeritageBusiness;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class HeritageBusinessRepository {

    private static final String COLLECTION_NAME = "HeritageBusinesses";

    public List<HeritageBusiness> findByStatus(String status) {
        Firestore firestore = FirestoreClient.getFirestore();

        try {
            return firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("status", status)
                    .get()
                    .get()
                    .getDocuments()
                    .stream()
                    .map(this::toHeritageBusiness)
                    .toList();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new FirebasePersistenceException(
                    "Loading heritage businesses was interrupted.",
                    exception
            );
        } catch (Exception exception) {
            throw new FirebasePersistenceException(
                    "Unable to load heritage businesses.",
                    exception
            );
        }
    }

    public List<HeritageBusiness> findByStatuses(List<String> statuses) {
        Firestore firestore = FirestoreClient.getFirestore();

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
            throw new FirebasePersistenceException(
                    "Loading reviewed heritage businesses was interrupted.",
                    exception
            );
        } catch (Exception exception) {
            throw new FirebasePersistenceException(
                    "Unable to load reviewed heritage businesses.",
                    exception
            );
        }
    }

    public Optional<HeritageBusiness> findById(String businessId) {
        Firestore firestore = FirestoreClient.getFirestore();

        try {
            DocumentSnapshot document = firestore.collection(COLLECTION_NAME)
                    .document(businessId)
                    .get()
                    .get();

            if (!document.exists()) {
                return Optional.empty();
            }

            return Optional.of(toHeritageBusiness(document));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new FirebasePersistenceException(
                    "Loading the heritage business was interrupted.",
                    exception
            );
        } catch (Exception exception) {
            throw new FirebasePersistenceException(
                    "Unable to load the heritage business.",
                    exception
            );
        }
    }

    public void approve(String businessId) {
        updateReviewStatus(businessId, "Approved", "approveAt");
    }

    public void reject(String businessId) {
        updateReviewStatus(businessId, "Rejected", "rejectAt");
    }

    private void updateReviewStatus(
            String businessId,
            String status,
            String timestampField) {

        Firestore firestore = FirestoreClient.getFirestore();
        Map<String, Object> updates = new LinkedHashMap<>();
        updates.put("status", status);
        updates.put(timestampField, FieldValue.serverTimestamp());

        try {
            firestore.collection(COLLECTION_NAME)
                    .document(businessId)
                    .update(updates)
                    .get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new FirebasePersistenceException(
                    "Updating the heritage business status was interrupted.",
                    exception
            );
        } catch (Exception exception) {
            throw new FirebasePersistenceException(
                    "Unable to update the heritage business status.",
                    exception
            );
        }
    }

    public synchronized void saveAll(List<Map<String, Object>> businesses) {
        Firestore firestore = FirestoreClient.getFirestore();

        try {
            int currentCount = firestore
                    .collection(COLLECTION_NAME)
                    .get()
                    .get()
                    .size();

            WriteBatch batch = firestore.batch();

            for (int index = 0; index < businesses.size(); index++) {
                String businessId = String.format(
                        "hb_%03d",
                        currentCount + index + 1
                );

                DocumentReference document = firestore
                        .collection(COLLECTION_NAME)
                        .document(businessId);

                Map<String, Object> storedBusiness =
                        new LinkedHashMap<>(businesses.get(index));
                storedBusiness.put("businessId", businessId);
                storedBusiness.put("createdAt", FieldValue.serverTimestamp());
                batch.set(document, storedBusiness);
            }

            batch.commit().get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new FirebasePersistenceException(
                    "Saving heritage businesses was interrupted.",
                    exception
            );
        } catch (Exception exception) {
            throw new FirebasePersistenceException(
                    "Unable to save heritage businesses.",
                    exception
            );
        }
    }

    private HeritageBusiness toHeritageBusiness(DocumentSnapshot document) {
        GeoPoint location = document.getGeoPoint("location");
        com.google.cloud.Timestamp createdAt = document.getTimestamp("createdAt");
        Instant createdAtInstant = createdAt == null
                ? null
                : Instant.ofEpochSecond(createdAt.getSeconds(), createdAt.getNanos());
        com.google.cloud.Timestamp approveAt = document.getTimestamp("approveAt");
        Instant approveAtInstant = approveAt == null
                ? null
                : Instant.ofEpochSecond(approveAt.getSeconds(), approveAt.getNanos());
        com.google.cloud.Timestamp rejectAt = document.getTimestamp("rejectAt");
        Instant rejectAtInstant = rejectAt == null
                ? null
                : Instant.ofEpochSecond(rejectAt.getSeconds(), rejectAt.getNanos());

        return new HeritageBusiness(
                document.getId(),
                document.getString("name"),
                document.getString("address"),
                document.getString("city"),
                document.getString("description"),
                location == null ? null : location.getLatitude(),
                location == null ? null : location.getLongitude(),
                document.getString("sourceVideoLink"),
                document.getString("status"),
                document.getDouble("averageRating"),
                createdAtInstant,
                approveAtInstant,
                rejectAtInstant
        );
    }
}
