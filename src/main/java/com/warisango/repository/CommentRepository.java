package com.warisango.repository;

import com.warisango.exception.FirebasePersistenceException;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.warisango.model.Comment;
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

    public List<Comment> findByReviewId(String reviewId) {
        List<Comment> comments = new ArrayList<>();

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

    public Comment findByCommentId(String commentId) {
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

    public void save(Comment comment) {
        if (comment == null || comment.commentId() == null || comment.commentId().isBlank()) {
            throw new IllegalArgumentException("Comment ID cannot be empty.");
        }

        try {
            DocumentReference document = firestore
                    .collection(COLLECTION)
                    .document(comment.commentId());

            document.set(convertCommentToDocument(comment)).get();
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to save comment: " + comment.commentId(), e);
        }
    }

    public void update(Comment comment) {
        if (comment == null || comment.commentId() == null || comment.commentId().isBlank()) {
            throw new IllegalArgumentException("Comment ID cannot be empty.");
        }

        try {
            firestore
                    .collection(COLLECTION)
                    .document(comment.commentId())
                    .update(convertCommentToUpdateDocument(comment))
                    .get();
        } catch (Exception e) {
            throw new FirebasePersistenceException("Failed to update comment: " + comment.commentId(), e);
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

    private Comment convertDocumentToComment(DocumentSnapshot document) {
        return new Comment(
                getString(document, "commentId"),
                getString(document, "reviewId"),
                getString(document, "touristId"),
                getString(document, "commentText"),
                getString(document, "replyToCommentId"),
                getString(document, "replyToTouristName"),
                ReviewDateFormatter.format(document.get("createdAt")),
                ReviewDateFormatter.format(document.get("updatedAt")),
                getString(document, "moderationStatus"));
    }

    private Map<String, Object> convertCommentToDocument(Comment comment) {
        Map<String, Object> data = new HashMap<>();
        data.put("commentId", comment.commentId());
        data.put("reviewId", comment.reviewId());
        data.put("touristId", comment.touristId());
        data.put("commentText", comment.commentText());
        data.put("moderationStatus", comment.moderationStatus());
        addReplyFields(data, comment);
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());
        return data;
    }

    private Map<String, Object> convertCommentToUpdateDocument(Comment comment) {
        Map<String, Object> data = new HashMap<>();
        data.put("commentId", comment.commentId());
        data.put("reviewId", comment.reviewId());
        data.put("touristId", comment.touristId());
        data.put("commentText", comment.commentText());
        data.put("moderationStatus", comment.moderationStatus());
        addReplyFields(data, comment);
        data.put("updatedAt", FieldValue.serverTimestamp());
        return data;
    }

    private void addReplyFields(Map<String, Object> data, Comment comment) {
        if (comment.replyToCommentId() != null && !comment.replyToCommentId().isBlank()) {
            data.put("replyToCommentId", comment.replyToCommentId());
        }
        if (comment.replyToTouristName() != null && !comment.replyToTouristName().isBlank()) {
            data.put("replyToTouristName", comment.replyToTouristName());
        }
    }

    private String getString(DocumentSnapshot document, String field) {
        String value = document.getString(field);
        return value == null ? "" : value;
    }

}
