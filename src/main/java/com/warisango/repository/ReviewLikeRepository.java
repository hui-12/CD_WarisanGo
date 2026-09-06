package com.warisango.repository;

import com.warisango.exception.FirebasePersistenceException;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.warisango.model.ReviewLike;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firestore access for the root-level reviewLikes collection.
 */
@Repository
public class ReviewLikeRepository {

    private static final String COLLECTION = "reviewLikes";

    private final Firestore firestore;

    public ReviewLikeRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<ReviewLike> findByReviewId(String reviewId) {
        List<ReviewLike> likes = new ArrayList<>();

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
            throw new FirebasePersistenceException("Failed to load likes for review: " + reviewId, e);
        }
    }

    public void save(ReviewLike like) {
        try {
            DocumentReference document = firestore
                    .collection(COLLECTION)
                    .document(like.likeId());

            document.set(convertLikeToDocument(like)).get();
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to save review like: " + like.likeId(), e);
        }
    }

    public void delete(String likeId) {
        try {
            firestore.collection(COLLECTION).document(likeId).delete().get();
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to delete review like: " + likeId, e);
        }
    }

    public String generateNextLikeId() {
        return firestore.collection(COLLECTION).document().getId();
    }

    private ReviewLike convertDocumentToLike(DocumentSnapshot document) {
        return new ReviewLike(
                getString(document, "likeId"),
                getString(document, "reviewId"),
                getString(document, "touristId"),
                getTimestampText(document, "createdAt")
        );
    }

    private Map<String, Object> convertLikeToDocument(ReviewLike like) {
        Map<String, Object> data = new HashMap<>();
        data.put("likeId", like.likeId());
        data.put("reviewId", like.reviewId());
        data.put("touristId", like.touristId());
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
