package com.warisango.repository;

import com.warisango.exception.FirebasePersistenceException;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.warisango.model.ReviewPhoto;
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

    public List<ReviewPhoto> findAll() {
        try {
            List<ReviewPhoto> photos = new ArrayList<>();
            QuerySnapshot snapshot = firestore.collection(COLLECTION).get().get();

            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                photos.add(convertDocumentToPhoto(document));
            }

            return photos;
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to load review photos.", e);
        }
    }

    public List<ReviewPhoto> findByReviewId(String reviewId) {
        List<ReviewPhoto> photos = new ArrayList<>();

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

    public void save(ReviewPhoto photo) {
        if (photo == null || photo.photoId() == null || photo.photoId().isBlank()) {
            throw new IllegalArgumentException("Photo ID cannot be empty.");
        }

        try {
            DocumentReference document = firestore
                    .collection(COLLECTION)
                    .document(photo.photoId());

            document.set(convertPhotoToDocument(photo)).get();
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to save review photo: " + photo.photoId(), e);
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

    private ReviewPhoto convertDocumentToPhoto(DocumentSnapshot document) {
        return new ReviewPhoto(
                getString(document, "photoId"),
                getString(document, "reviewId"),
                getString(document, "photoUrl"),
                getString(document, "storagePath")
        );
    }

    private Map<String, Object> convertPhotoToDocument(ReviewPhoto photo) {
        Map<String, Object> data = new HashMap<>();
        data.put("photoId", photo.photoId());
        data.put("reviewId", photo.reviewId());
        data.put("photoUrl", photo.photoUrl());
        data.put("storagePath", photo.storagePath());
        return data;
    }

    private String getString(DocumentSnapshot document, String field) {
        String value = document.getString(field);
        return value == null ? "" : value;
    }
}
