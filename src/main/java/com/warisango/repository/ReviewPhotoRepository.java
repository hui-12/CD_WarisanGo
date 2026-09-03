package com.warisango.repository;

import com.warisango.exception.FirebasePersistenceException;

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
 * Firestore access for the root-level reviewPhotos collection.
 */
@Repository
public class ReviewPhotoRepository {

    private static final String COLLECTION = "reviewPhotos";

    private final Firestore firestore;

    public ReviewPhotoRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<ReviewPhotoDTO> findAll() {
        try {
            List<ReviewPhotoDTO> photos = new ArrayList<>();
            QuerySnapshot snapshot = firestore.collection(COLLECTION).get().get();

            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                photos.add(convertDocumentToPhoto(document));
            }

            return photos;
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to load review photos.", e);
        }
    }

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
            throw new FirebasePersistenceException("Failed to load photos for review: " + reviewId, e);
        }
    }

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
            throw new FirebasePersistenceException("Failed to save review photo: " + photo.getPhotoId(), e);
        }
    }

    public void delete(String photoId) {
        try {
            firestore.collection(COLLECTION).document(photoId).delete().get();
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to delete review photo: " + photoId, e);
        }
    }

    public String generateNextPhotoId() {
        return firestore.collection(COLLECTION).document().getId();
    }

    private ReviewPhotoDTO convertDocumentToPhoto(DocumentSnapshot document) {
        return new ReviewPhotoDTO(
                getString(document, "photoId"),
                getString(document, "reviewId"),
                getString(document, "photoUrl"),
                getString(document, "storagePath")
        );
    }

    private Map<String, Object> convertPhotoToDocument(ReviewPhotoDTO photo) {
        Map<String, Object> data = new HashMap<>();
        data.put("photoId", photo.getPhotoId());
        data.put("reviewId", photo.getReviewId());
        data.put("photoUrl", photo.getPhotoUrl());
        data.put("storagePath", photo.getStoragePath());
        return data;
    }

    private String getString(DocumentSnapshot document, String field) {
        String value = document.getString(field);
        return value == null ? "" : value;
    }
}
