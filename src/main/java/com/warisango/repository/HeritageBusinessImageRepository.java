package com.warisango.repository;

import com.warisango.exception.FirebasePersistenceException;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.warisango.model.BusinessPhoto;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Instant;

@Repository
public class HeritageBusinessImageRepository {

    private static final String IMAGE_COLLECTION = "heritageBusinessImages";
    private static final String ACTIVE = "ACTIVE";
    private final Firestore firestore;

    public HeritageBusinessImageRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<String> findImageUrlsByBusinessId(String businessId) {
        return findByBusinessId(businessId).stream().map(BusinessPhoto::imageUrl).toList();
    }

    public List<BusinessPhoto> findByBusinessId(String businessId) {
        try {
            return firestore.collection(IMAGE_COLLECTION)
                    .whereEqualTo("businessId", businessId)
                    .get()
                    .get()
                    .getDocuments()
                    .stream()
                    .map(this::toModel)
                    .filter(photo -> photo != null && !"REMOVED".equalsIgnoreCase(photo.status()))
                    .sorted(Comparator.comparingLong(BusinessPhoto::displayOrder))
                    .toList();
        } catch (Exception exception) {
            throw new FirebasePersistenceException("Failed to load images for business: " + businessId, exception);
        }
    }

    public BusinessPhoto findById(String photoId) {
        try {
            DocumentSnapshot document = firestore.collection(IMAGE_COLLECTION).document(photoId).get().get();
            return document.exists() ? toModel(document) : null;
        } catch (Exception exception) {
            throw new FirebasePersistenceException("Failed to load business photo: " + photoId, exception);
        }
    }

    public String save(String businessId, String imageUrl, String storagePath, String uploadedBy) {
        try {
            var reference = firestore.collection(IMAGE_COLLECTION).document();
            Map<String, Object> data = new HashMap<>();
            data.put("photoId", reference.getId());
            data.put("businessId", businessId);
            data.put("imageUrl", imageUrl);
            data.put("storagePath", storagePath);
            data.put("uploadedBy", uploadedBy);
            data.put("uploadedAt", FieldValue.serverTimestamp());
            data.put("displayOrder", System.currentTimeMillis());
            data.put("status", ACTIVE);
            reference.set(data).get();
            return reference.getId();
        } catch (Exception exception) {
            throw new FirebasePersistenceException("Failed to save the business photo.", exception);
        }
    }

    public void markRemoved(String photoId) {
        try {
            firestore.collection(IMAGE_COLLECTION).document(photoId)
                    .update(Map.of("status", "REMOVED", "removedAt", FieldValue.serverTimestamp())).get();
        } catch (Exception exception) {
            throw new FirebasePersistenceException("Failed to remove the business photo.", exception);
        }
    }

    private BusinessPhoto toModel(DocumentSnapshot document) {
        String imageUrl = document.getString("imageUrl");
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        Long order = document.getLong("displayOrder");
        return new BusinessPhoto(
                valueOrDefault(document.getString("photoId"), document.getId()),
                document.getString("businessId"), imageUrl, document.getString("storagePath"),
                document.getString("uploadedBy"), toInstant(document, "uploadedAt"),
                order == null ? Long.MAX_VALUE : order,
                valueOrDefault(document.getString("status"), ACTIVE));
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private Instant toInstant(DocumentSnapshot document, String field) {
        var timestamp = document.getTimestamp(field);
        return timestamp == null ? null : timestamp.toDate().toInstant();
    }

}
