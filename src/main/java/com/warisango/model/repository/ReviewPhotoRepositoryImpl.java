package com.warisango.model.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.warisango.dto.ReviewPhotoDTO;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firestore access for the root-level ReviewPhotos collection.
 */
@Repository
public class ReviewPhotoRepositoryImpl implements ReviewPhotoRepository {

    private static final String COLLECTION = "ReviewPhotos";

    private final Firestore firestore;

    public ReviewPhotoRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public List<ReviewPhotoDTO> findByReviewId(String reviewId) {
        List<ReviewPhotoDTO> photos = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> future = firestore
                    .collection(COLLECTION)
                    .whereEqualTo("reviewId", reviewId)
                    .get();

            for (QueryDocumentSnapshot document : future.get().getDocuments()) {
                photos.add(convertDocumentToPhoto(document));
            }

            return photos;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load photos for review: " + reviewId, e);
        }
    }

    @Override
    public void save(ReviewPhotoDTO photo) {
        if (photo == null || photo.getPhotoId() == null || photo.getPhotoId().isBlank()) {
            throw new IllegalArgumentException("Photo ID cannot be empty.");
        }

        try {
            DocumentReference document = firestore
                    .collection(COLLECTION)
                    .document(photo.getPhotoId());

            document.set(convertPhotoToDocument(photo)).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save review photo: " + photo.getPhotoId(), e);
        }
    }

    @Override
    public void delete(String photoId) {
        try {
            firestore.collection(COLLECTION).document(photoId).delete().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete review photo: " + photoId, e);
        }
    }

    @Override
    public String generateNextPhotoId() {
        try {
            List<QueryDocumentSnapshot> documents = firestore
                    .collection(COLLECTION)
                    .get()
                    .get()
                    .getDocuments();

            int maxNumber = 0;

            for (QueryDocumentSnapshot document : documents) {
                String photoId = document.getString("photoId");

                if (photoId == null || !photoId.startsWith("photo_")) {
                    continue;
                }

                try {
                    int number = Integer.parseInt(photoId.substring("photo_".length()));
                    maxNumber = Math.max(maxNumber, number);
                } catch (NumberFormatException ignored) {
                    // Ignore IDs that do not follow the photo_001 format.
                }
            }

            return String.format("photo_%03d", maxNumber + 1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate next photo ID.", e);
        }
    }

    private ReviewPhotoDTO convertDocumentToPhoto(DocumentSnapshot document) {
        return new ReviewPhotoDTO(
                getString(document, "photoId"),
                getString(document, "reviewId"),
                getString(document, "photoUrl")
        );
    }

    private Map<String, Object> convertPhotoToDocument(ReviewPhotoDTO photo) {
        Map<String, Object> data = new HashMap<>();
        data.put("photoId", photo.getPhotoId());
        data.put("reviewId", photo.getReviewId());
        data.put("photoUrl", photo.getPhotoUrl());
        return data;
    }

    private String getString(DocumentSnapshot document, String field) {
        String value = document.getString(field);
        return value == null ? "" : value;
    }
}
