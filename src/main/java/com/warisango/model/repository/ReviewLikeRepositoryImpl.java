package com.warisango.model.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.warisango.dto.ReviewLikeDTO;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firestore access for the root-level ReviewLikes collection.
 */
@Repository
public class ReviewLikeRepositoryImpl implements ReviewLikeRepository {

    private static final String COLLECTION = "ReviewLikes";

    private final Firestore firestore;

    public ReviewLikeRepositoryImpl() {
        this.firestore = FirestoreClient.getFirestore();
    }

    @Override
    public List<ReviewLikeDTO> findByReviewId(String reviewId) {
        List<ReviewLikeDTO> likes = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> future = firestore
                    .collection(COLLECTION)
                    .whereEqualTo("reviewId", reviewId)
                    .get();

            for (QueryDocumentSnapshot document : future.get().getDocuments()) {
                likes.add(convertDocumentToLike(document));
            }

            return likes;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load likes for review: " + reviewId, e);
        }
    }

    @Override
    public void save(ReviewLikeDTO like) {
        try {
            DocumentReference document = firestore
                    .collection(COLLECTION)
                    .document(like.getLikeId());

            document.set(convertLikeToDocument(like)).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save review like: " + like.getLikeId(), e);
        }
    }

    @Override
    public void delete(String likeId) {
        try {
            firestore.collection(COLLECTION).document(likeId).delete().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete review like: " + likeId, e);
        }
    }

    @Override
    public String generateNextLikeId() {
        try {
            List<QueryDocumentSnapshot> documents = firestore
                    .collection(COLLECTION)
                    .get()
                    .get()
                    .getDocuments();

            int maxNumber = 0;

            for (QueryDocumentSnapshot document : documents) {
                String likeId = document.getString("likeId");

                if (likeId == null || !likeId.startsWith("reviewLike_")) {
                    continue;
                }

                try {
                    int number = Integer.parseInt(likeId.substring("reviewLike_".length()));
                    maxNumber = Math.max(maxNumber, number);
                } catch (NumberFormatException ignored) {
                    // Ignore IDs that do not follow the reviewLike_001 format.
                }
            }

            return String.format("reviewLike_%03d", maxNumber + 1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate next review like ID.", e);
        }
    }

    private ReviewLikeDTO convertDocumentToLike(DocumentSnapshot document) {
        return new ReviewLikeDTO(
                getString(document, "likeId"),
                getString(document, "reviewId"),
                getString(document, "touristId"),
                getTimestampText(document, "createdAt")
        );
    }

    private Map<String, Object> convertLikeToDocument(ReviewLikeDTO like) {
        Map<String, Object> data = new HashMap<>();
        data.put("likeId", like.getLikeId());
        data.put("reviewId", like.getReviewId());
        data.put("touristId", like.getTouristId());
        data.put("createdAt", FieldValue.serverTimestamp());
        return data;
    }

    private String getString(DocumentSnapshot document, String field) {
        String value = document.getString(field);
        return value == null ? "" : value;
    }

    private String getTimestampText(DocumentSnapshot document, String field) {
        Object value = document.get(field);
        return value == null ? "" : value.toString();
    }
}
