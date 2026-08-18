package com.warisango.model.service;

import com.warisango.dto.CommentDTO;
import com.warisango.dto.ReviewDTO;
import com.warisango.model.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Handles comment business rules and delegates persistence to CommentRepository.
 */
@Service
public class CommentService {

    private static final int PREVIEW_LIMIT = 2;

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<CommentDTO> getCommentsByReview(String reviewId) {
        return commentRepository.findByReviewId(reviewId);
    }

    public Map<String, List<CommentDTO>> getCommentPreviews(Collection<ReviewDTO> reviews) {
        Map<String, List<CommentDTO>> previews = new LinkedHashMap<>();

        if (reviews == null) {
            return previews;
        }

        for (ReviewDTO review : reviews) {
            if (review == null || review.getReviewId() == null || review.getReviewId().isBlank()) {
                continue;
            }

            List<CommentDTO> comments = getCommentsByReview(review.getReviewId());
            previews.put(review.getReviewId(), new ArrayList<>(
                    comments.subList(0, Math.min(PREVIEW_LIMIT, comments.size()))
            ));
        }

        return previews;
    }

    public CommentDTO getComment(String commentId) {
        return commentRepository.findByCommentId(commentId);
    }

    public void createComment(CommentDTO comment, String currentUserId) {
        comment.setCommentId(commentRepository.generateNextCommentId());
        comment.setTouristId(currentUserId);
        comment.setTouristName(currentUserId);
        comment.setCreatedAt(LocalDateTime.now().toLocalDate().toString());
        comment.setUpdatedAt(LocalDateTime.now().toLocalDate().toString());

        commentRepository.save(comment);
    }

    public boolean updateComment(CommentDTO submittedComment, String currentUserId) {
        CommentDTO existingComment = commentRepository.findByCommentId(submittedComment.getCommentId());

        if (existingComment == null || !isCommentOwner(existingComment, currentUserId)) {
            return false;
        }

        submittedComment.setCommentId(existingComment.getCommentId());
        submittedComment.setReviewId(existingComment.getReviewId());
        submittedComment.setTouristId(existingComment.getTouristId());
        submittedComment.setTouristName(existingComment.getTouristName());
        submittedComment.setCreatedAt(existingComment.getCreatedAt());
        submittedComment.setUpdatedAt(LocalDateTime.now().toLocalDate().toString());

        commentRepository.update(submittedComment);
        return true;
    }

    public boolean deleteComment(String commentId, String currentUserId) {
        CommentDTO existingComment = commentRepository.findByCommentId(commentId);

        if (existingComment == null || !isCommentOwner(existingComment, currentUserId)) {
            return false;
        }

        commentRepository.delete(commentId);
        return true;
    }

    public boolean isCommentOwner(CommentDTO comment, String currentUserId) {
        return comment != null && Objects.equals(comment.getTouristId(), currentUserId);
    }
}
