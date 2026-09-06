package com.warisango.repository;

import com.warisango.exception.FirebasePersistenceException;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.warisango.model.CommentLike;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firestore access for the root-level commentLikes collection.
 */
@Repository
public class CommentLikeRepository {

    private static final String COLLECTION = "commentLikes";

    private final Firestore firestore;

    public CommentLikeRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<CommentLike> findByCommentId(String commentId) {
        List<CommentLike> likes = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> future = firestore
                    .collection(COLLECTION)
                    .whereEqualTo("commentId", commentId)
                    .get();

            for (QueryDocumentSnapshot document : future.get().getDocuments()) {
                likes.add(convertDocumentToLike(document));
            }

            return likes;
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to load likes for comment: " + commentId, e);
        }
    }

    public void save(CommentLike like) {
        try {
            DocumentReference document = firestore
                    .collection(COLLECTION)
                    .document(like.likeId());

            document.set(convertLikeToDocument(like)).get();
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to save comment like: " + like.likeId(), e);
        }
    }

    public void delete(String likeId) {
        try {
            firestore.collection(COLLECTION).document(likeId).delete().get();
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to delete comment like: " + likeId, e);
        }
    }

    public String generateNextLikeId() {
        return firestore.collection(COLLECTION).document().getId();
    }

    private CommentLike convertDocumentToLike(DocumentSnapshot document) {
        return new CommentLike(
                getString(document, "likeId"),
                getString(document, "commentId"),
                getString(document, "touristId"),
                getTimestampText(document, "createdAt")
        );
    }

    private Map<String, Object> convertLikeToDocument(CommentLike like) {
        Map<String, Object> data = new HashMap<>();
        data.put("likeId", like.likeId());
        data.put("commentId", like.commentId());
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
