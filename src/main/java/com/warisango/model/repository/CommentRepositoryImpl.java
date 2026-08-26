package com.warisango.model.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.warisango.dto.CommentDTO;
import com.warisango.util.ReviewDateFormatter;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firestore access for the root-level Comments collection.
 */
@Repository
public class CommentRepositoryImpl implements CommentRepository {

    private static final String COLLECTION = "Comments";

    private final Firestore firestore;

    public CommentRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public List<CommentDTO> findByReviewId(String reviewId) {
        List<CommentDTO> comments = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> future = firestore
                    .collection(COLLECTION)
                    .whereEqualTo("reviewId", reviewId)
                    .get();

            for (QueryDocumentSnapshot document : future.get().getDocuments()) {
                comments.add(convertDocumentToComment(document));
            }

            return comments;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load comments for review: " + reviewId, e);
        }
    }

    @Override
    public CommentDTO findByCommentId(String commentId) {
        try {
            DocumentSnapshot document = firestore
                    .collection(COLLECTION)
                    .document(commentId)
                    .get()
                    .get();

            return document.exists() ? convertDocumentToComment(document) : null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load comment: " + commentId, e);
        }
    }

    @Override
    public void save(CommentDTO comment) {
        if (comment == null || comment.getCommentId() == null || comment.getCommentId().isBlank()) {
            throw new IllegalArgumentException("Comment ID cannot be empty.");
        }

        try {
            DocumentReference document = firestore
                    .collection(COLLECTION)
                    .document(comment.getCommentId());

            document.set(convertCommentToDocument(comment)).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save comment: " + comment.getCommentId(), e);
        }
    }

    @Override
    public void update(CommentDTO comment) {
        if (comment == null || comment.getCommentId() == null || comment.getCommentId().isBlank()) {
            throw new IllegalArgumentException("Comment ID cannot be empty.");
        }

        try {
            firestore
                    .collection(COLLECTION)
                    .document(comment.getCommentId())
                    .update(convertCommentToUpdateDocument(comment))
                    .get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update comment: " + comment.getCommentId(), e);
        }
    }

    @Override
    public void delete(String commentId) {
        try {
            firestore.collection(COLLECTION).document(commentId).delete().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete comment: " + commentId, e);
        }
    }

    @Override
    public String generateNextCommentId() {
        try {
            List<QueryDocumentSnapshot> documents = firestore
                    .collection(COLLECTION)
                    .get()
                    .get()
                    .getDocuments();

            int maxNumber = 0;

            for (QueryDocumentSnapshot document : documents) {
                String commentId = document.getString("commentId");

                if (commentId == null || !commentId.startsWith("comment_")) {
                    continue;
                }

                try {
                    int number = Integer.parseInt(commentId.substring("comment_".length()));
                    maxNumber = Math.max(maxNumber, number);
                } catch (NumberFormatException ignored) {
                    // Ignore IDs that do not follow the comment_001 format.
                }
            }

            return String.format("comment_%03d", maxNumber + 1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate next comment ID.", e);
        }
    }

    private CommentDTO convertDocumentToComment(DocumentSnapshot document) {
        CommentDTO comment = new CommentDTO();
        comment.setCommentId(getString(document, "commentId"));
        comment.setReviewId(getString(document, "reviewId"));
        comment.setTouristId(getString(document, "touristId"));
        // Tourist/User lookup will replace this temporary display value later.
        comment.setTouristName(comment.getTouristId());
        comment.setCommentText(getString(document, "commentText"));
        comment.setReplyToCommentId(getString(document, "replyToCommentId"));
        comment.setReplyToTouristName(getString(document, "replyToTouristName"));
        comment.setModerationStatus(getString(document, "moderationStatus"));
        comment.setCreatedAt(ReviewDateFormatter.format(document.get("createdAt")));
        comment.setUpdatedAt(ReviewDateFormatter.format(document.get("updatedAt")));
        return comment;
    }

    private Map<String, Object> convertCommentToDocument(CommentDTO comment) {
        Map<String, Object> data = new HashMap<>();
        data.put("commentId", comment.getCommentId());
        data.put("reviewId", comment.getReviewId());
        data.put("touristId", comment.getTouristId());
        data.put("commentText", comment.getCommentText());
        data.put("moderationStatus", comment.getModerationStatus());
        addReplyFields(data, comment);
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());
        return data;
    }

    private Map<String, Object> convertCommentToUpdateDocument(CommentDTO comment) {
        Map<String, Object> data = new HashMap<>();
        data.put("commentId", comment.getCommentId());
        data.put("reviewId", comment.getReviewId());
        data.put("touristId", comment.getTouristId());
        data.put("commentText", comment.getCommentText());
        data.put("moderationStatus", comment.getModerationStatus());
        addReplyFields(data, comment);
        data.put("updatedAt", FieldValue.serverTimestamp());
        return data;
    }

    private void addReplyFields(Map<String, Object> data, CommentDTO comment) {
        if (comment.getReplyToCommentId() != null && !comment.getReplyToCommentId().isBlank()) {
            data.put("replyToCommentId", comment.getReplyToCommentId());
        }
        if (comment.getReplyToTouristName() != null && !comment.getReplyToTouristName().isBlank()) {
            data.put("replyToTouristName", comment.getReplyToTouristName());
        }
    }

    private String getString(DocumentSnapshot document, String field) {
        String value = document.getString(field);
        return value == null ? "" : value;
    }

}
