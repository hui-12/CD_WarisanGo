package com.warisango.repository;

import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.warisango.model.SavedListing;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class SavedListingRepository {
    private static final String SAVED_LISTINGS = "savedListings";

    private final Firestore firestore;

    public SavedListingRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<SavedListing> findByTouristId(String touristId) throws Exception {
        return firestore.collection(SAVED_LISTINGS)
                .whereEqualTo("touristId", touristId)
                .get()
                .get()
                .getDocuments()
                .stream()
                .map(document -> document.toObject(SavedListing.class))
                .filter(Objects::nonNull)
                .filter(savedListing -> savedListing.getBusinessId() != null)
                .sorted((left, right) -> compareSavedAt(right, left))
                .toList();
    }

    public boolean exists(String touristId, String businessId) throws Exception {
        return firestore.collection(SAVED_LISTINGS)
                .document(documentId(touristId, businessId))
                .get()
                .get()
                .exists();
    }

    public void save(String touristId, String businessId) throws Exception {
        String savedListingId = documentId(touristId, businessId);
        Map<String, Object> data = new HashMap<>();
        data.put("savedListingId", savedListingId);
        data.put("touristId", touristId);
        data.put("businessId", businessId);
        data.put("savedAt", FieldValue.serverTimestamp());
        firestore.collection(SAVED_LISTINGS).document(savedListingId).set(data).get();
    }

    public void delete(String touristId, String businessId) throws Exception {
        firestore.collection(SAVED_LISTINGS)
                .document(documentId(touristId, businessId))
                .delete()
                .get();
    }

    private int compareSavedAt(SavedListing left, SavedListing right) {
        if (left.getSavedAt() == null && right.getSavedAt() == null) return 0;
        if (left.getSavedAt() == null) return 1;
        if (right.getSavedAt() == null) return -1;
        return left.getSavedAt().compareTo(right.getSavedAt());
    }

    private String documentId(String touristId, String businessId) {
        return touristId + "_" + businessId;
    }
}
