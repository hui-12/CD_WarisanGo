package com.warisango.model.repository;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Repository
public class HeritageBusinessImageRepository {

    private static final String COLLECTION_NAME = "heritageBusinessImages";
    private static final int WHERE_IN_LIMIT = 30;

    private final Firestore firestore;

    public HeritageBusinessImageRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Map<String, List<String>> findImageUrlsByBusinessIds(Collection<String> businessIds)
            throws ExecutionException, InterruptedException {
        List<String> ids = businessIds.stream()
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .distinct()
                .toList();

        Map<String, List<ImageReference>> imagesByBusinessId = new HashMap<>();
        for (int start = 0; start < ids.size(); start += WHERE_IN_LIMIT) {
            List<String> batch = ids.subList(start, Math.min(start + WHERE_IN_LIMIT, ids.size()));
            QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                    .whereIn("businessId", batch)
                    .get()
                    .get();

            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                String businessId = document.getString("businessId");
                String imageUrl = document.getString("imageUrl");
                if (businessId == null || businessId.isBlank() || imageUrl == null || imageUrl.isBlank()) {
                    continue;
                }
                imagesByBusinessId.computeIfAbsent(businessId, ignored -> new ArrayList<>())
                        .add(new ImageReference(imageUrl, readDisplayOrder(document)));
            }
        }

        Map<String, List<String>> urlsByBusinessId = new LinkedHashMap<>();
        for (String businessId : ids) {
            List<ImageReference> images = imagesByBusinessId.getOrDefault(businessId, List.of());
            urlsByBusinessId.put(businessId, images.stream()
                    .sorted(Comparator.comparingLong(ImageReference::displayOrder))
                    .map(ImageReference::imageUrl)
                    .toList());
        }
        return urlsByBusinessId;
    }

    private long readDisplayOrder(DocumentSnapshot document) {
        Object displayOrder = document.get("displayOrder");
        return displayOrder instanceof Number number ? number.longValue() : Long.MAX_VALUE;
    }

    private record ImageReference(String imageUrl, long displayOrder) {
    }
}
