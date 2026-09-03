package com.warisango.repository;

import com.warisango.exception.FirebasePersistenceException;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;

@Repository
public class HeritageBusinessImageRepository {

    private static final String IMAGE_COLLECTION = "heritageBusinessImages";
    private final Firestore firestore;

    public HeritageBusinessImageRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<String> findImageUrlsByBusinessId(String businessId) {
        try {
            return firestore.collection(IMAGE_COLLECTION)
                    .whereEqualTo("businessId", businessId)
                    .get()
                    .get()
                    .getDocuments()
                    .stream()
                    .sorted(Comparator.comparingLong(this::displayOrder))
                    .map(document -> document.getString("imageUrl"))
                    .filter(url -> url != null && !url.isBlank())
                    .toList();
        } catch (Exception exception) {
            throw new FirebasePersistenceException("Failed to load images for business: " + businessId, exception);
        }
    }

    private long displayOrder(DocumentSnapshot document) {
        Long order = document.getLong("displayOrder");
        return order == null ? Long.MAX_VALUE : order;
    }
}
