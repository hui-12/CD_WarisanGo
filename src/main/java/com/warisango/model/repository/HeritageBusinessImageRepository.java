package com.warisango.model.repository;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class HeritageBusinessImageRepository {

    private static final String IMAGE_COLLECTION = "heritageBusinessImages";
    private static final String BUSINESS_COLLECTION = "heritageBusinesses";
    private static final String LEGACY_BUSINESS_COLLECTION = "HeritageBusinesses";
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
            throw new RuntimeException("Failed to load images for business: " + businessId, exception);
        }
    }

    public void saveIfAbsent(String imageId, String businessId, String imageUrl, int displayOrder) {
        try {
            var reference = firestore.collection(IMAGE_COLLECTION).document(imageId);
            if (reference.get().get().exists()) {
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("imageId", imageId);
            data.put("businessId", businessId);
            data.put("imageUrl", imageUrl);
            data.put("displayOrder", displayOrder);
            reference.set(data).get();
        } catch (Exception exception) {
            throw new RuntimeException("Failed to save heritage business image: " + imageId, exception);
        }
    }

    public int migrateEmbeddedImagesAndRemoveDeprecatedFields() {
        try {
            int migrated = migrateEmbeddedImagesFrom(LEGACY_BUSINESS_COLLECTION);
            migrateEmbeddedImagesFrom(BUSINESS_COLLECTION);

            for (DocumentSnapshot document : firestore.collection(BUSINESS_COLLECTION).get().get().getDocuments()) {
                document.getReference().set(Map.of(
                        "photos", FieldValue.delete()
                ), SetOptions.merge()).get();
            }
            return migrated;
        } catch (Exception exception) {
            throw new RuntimeException("Failed to migrate heritage business images.", exception);
        }
    }

    private int migrateEmbeddedImagesFrom(String collectionName) throws Exception {
        int migrated = 0;
        for (DocumentSnapshot document : firestore.collection(collectionName).get().get().getDocuments()) {
            String storedId = document.getString("businessId");
            String businessId = storedId == null || storedId.isBlank() ? document.getId() : storedId.trim();
            Object value = document.get("photos");
            if (!(value instanceof List<?> photos)) {
                continue;
            }

            List<String> urls = new ArrayList<>();
            for (Object photo : photos) {
                if (photo instanceof String url && !url.isBlank()) {
                    urls.add(url.trim());
                }
            }
            for (int index = 0; index < urls.size(); index++) {
                saveIfAbsent(businessId + "_image_" + (index + 1), businessId, urls.get(index), index);
                migrated++;
            }
        }
        return migrated;
    }

    private long displayOrder(DocumentSnapshot document) {
        Long order = document.getLong("displayOrder");
        return order == null ? Long.MAX_VALUE : order;
    }
}
