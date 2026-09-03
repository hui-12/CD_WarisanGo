package com.warisango.repository;

import com.warisango.exception.FirebasePersistenceException;

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
 * Firestore access for the root-level comments collection.
 */
@Repository
public class CommentRepository {

    private static final String COLLECTION = "comments";

    private final Firestore firestore;

    public CommentRepository(Firestore firestore) {
        this.firestore = firestore;
    }

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
            throw new FirebasePersistenceException("Failed to load comments for review: " + reviewId, e);
        }
    }

    public CommentDTO findByCommentId(String commentId) {
        try {
            DocumentSnapshot document = firestore
                    .collection(COLLECTION)
                    .document(commentId)
                    .get()
                    .get();

            return document.exists() ? convertDocumentToComment(document) : null;
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to load comment: " + commentId, e);
        }
    }

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
            throw new FirebasePersistenceException("Failed to save comment: " + comment.getCommentId(), e);
        }
    }

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
            throw new FirebasePersistenceException("Failed to update comment: " + comment.getCommentId(), e);
        }
    }

    public void delete(String commentId) {
        try {
            firestore.collection(COLLECTION).document(commentId).delete().get();
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to delete comment: " + commentId, e);
        }
    }

    public String generateNextCommentId() {
        return firestore.collection(COLLECTION).document().getId();
    }

    private CommentDTO convertDocumentToComment(DocumentSnapshot document) {
        CommentDTO comment = new CommentDTO();
        comment.setCommentId(getString(document, "commentId"));
        comment.setReviewId(getString(document, "reviewId"));
        comment.setTouristId(getString(document, "touristId"));
        // CommentService resolves this Firebase user UID through the users collection.
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
