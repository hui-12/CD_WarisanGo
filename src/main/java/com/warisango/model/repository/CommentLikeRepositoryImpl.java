package com.warisango.model.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.warisango.dto.CommentLikeDTO;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firestore access for the root-level commentLikes collection.
 */
@Repository
public class CommentLikeRepositoryImpl implements CommentLikeRepository {

    private static final String COLLECTION = "commentLikes";

    private final Firestore firestore;

    public CommentLikeRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public List<CommentLikeDTO> findByCommentId(String commentId) {
        List<CommentLikeDTO> likes = new ArrayList<>();

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
            throw new RuntimeException("Failed to load likes for comment: " + commentId, e);
        }
    }

    @Override
    public void save(CommentLikeDTO like) {
        try {
            DocumentReference document = firestore
                    .collection(COLLECTION)
                    .document(like.getLikeId());

            document.set(convertLikeToDocument(like)).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save comment like: " + like.getLikeId(), e);
        }
    }

    @Override
    public void delete(String likeId) {
        try {
            firestore.collection(COLLECTION).document(likeId).delete().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete comment like: " + likeId, e);
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

                if (likeId == null || !likeId.startsWith("commentLike_")) {
                    continue;
                }

                try {
                    int number = Integer.parseInt(likeId.substring("commentLike_".length()));
                    maxNumber = Math.max(maxNumber, number);
                } catch (NumberFormatException ignored) {
                    // Ignore IDs that do not follow the commentLike_001 format.
                }
            }

            return String.format("commentLike_%03d", maxNumber + 1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate next comment like ID.", e);
        }
    }

    private CommentLikeDTO convertDocumentToLike(DocumentSnapshot document) {
        return new CommentLikeDTO(
                getString(document, "likeId"),
                getString(document, "commentId"),
                getString(document, "touristId"),
                getTimestampText(document, "createdAt")
        );
    }

    private Map<String, Object> convertLikeToDocument(CommentLikeDTO like) {
        Map<String, Object> data = new HashMap<>();
        data.put("likeId", like.getLikeId());
        data.put("commentId", like.getCommentId());
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
