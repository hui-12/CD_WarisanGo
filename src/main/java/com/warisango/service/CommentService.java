package com.warisango.service;

import com.warisango.dto.CommentDTO;
import com.warisango.dto.ReviewDTO;
import com.warisango.model.Comment;
import com.warisango.repository.CommentLikeRepository;
import com.warisango.repository.CommentRepository;
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
    private final CommentLikeRepository commentLikeRepository;
    private final ContentModerationService contentModerationService;
    private final UserService userService;

    public CommentService(
            CommentRepository commentRepository,
            CommentLikeRepository commentLikeRepository,
            ContentModerationService contentModerationService,
            UserService userService) {
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.contentModerationService = contentModerationService;
        this.userService = userService;
    }

    public List<CommentDTO> getCommentsByReview(String reviewId) {
        List<CommentDTO> comments = commentRepository.findByReviewId(reviewId).stream()
                .map(this::toDto)
                .filter(this::isVisible)
                .toList();
        enrichDisplayNames(comments);
        return comments;
    }

    public List<CommentDTO> getCommentsByReviewIncludingHidden(String reviewId) {
        List<CommentDTO> comments = commentRepository.findByReviewId(reviewId).stream().map(this::toDto).toList();
        enrichDisplayNames(comments);
        return comments;
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
        CommentDTO comment = toDto(commentRepository.findByCommentId(commentId));
        enrichDisplayNames(comment == null ? List.of() : List.of(comment));
        return isVisible(comment) ? comment : null;
    }

    public CommentDTO getCommentIncludingHidden(String commentId) {
        CommentDTO comment = toDto(commentRepository.findByCommentId(commentId));
        enrichDisplayNames(comment == null ? List.of() : List.of(comment));
        return comment;
    }

    public void createComment(CommentDTO comment, String currentUserId) {
        createComment(comment, currentUserId, comment.getReplyToCommentId());
    }

    public void createComment(CommentDTO comment, String currentUserId, String requestedReplyId) {
        if (currentUserId == null || currentUserId.isBlank()) {
            throw new IllegalArgumentException("Authenticated user ID is required.");
        }

        contentModerationService.validate(comment.getCommentText());
        applyReplyTarget(comment, requestedReplyId);
        comment.setCommentId(commentRepository.generateNextCommentId());
        comment.setTouristId(currentUserId);
        comment.setTouristName(userService.getDisplayNameByUserId(currentUserId));
        comment.setCreatedAt(LocalDateTime.now().toLocalDate().toString());
        comment.setUpdatedAt(LocalDateTime.now().toLocalDate().toString());

        commentRepository.save(toModel(comment));
    }

    public boolean updateComment(CommentDTO submittedComment, String currentUserId) {
        CommentDTO existingComment = toDto(commentRepository.findByCommentId(submittedComment.getCommentId()));

        if (existingComment == null || !isCommentOwner(existingComment, currentUserId)) {
            return false;
        }

        contentModerationService.validate(submittedComment.getCommentText());

        submittedComment.setCommentId(existingComment.getCommentId());
        submittedComment.setReviewId(existingComment.getReviewId());
        submittedComment.setTouristId(existingComment.getTouristId());
        submittedComment.setTouristName(displayName(existingComment));
        submittedComment.setReplyToCommentId(existingComment.getReplyToCommentId());
        submittedComment.setReplyToTouristName(replyTargetName(existingComment));
        submittedComment.setCreatedAt(existingComment.getCreatedAt());
        submittedComment.setUpdatedAt(LocalDateTime.now().toLocalDate().toString());

        commentRepository.update(toModel(submittedComment));
        return true;
    }

    public boolean deleteComment(String commentId, String currentUserId) {
        CommentDTO existingComment = toDto(commentRepository.findByCommentId(commentId));

        if (existingComment == null || !isCommentOwner(existingComment, currentUserId)) {
            return false;
        }

        commentRepository.delete(commentId);
        deleteCommentLikes(commentId);
        return true;
    }

    public void hideComment(String commentId) {
        CommentDTO comment = getCommentIncludingHidden(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("Comment was not found.");
        }

        comment.setModerationStatus("HIDDEN");
        commentRepository.update(toModel(comment));
    }

    public void restoreComment(String commentId) {
        CommentDTO comment = getCommentIncludingHidden(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("Comment was not found.");
        }

        comment.setModerationStatus("VISIBLE");
        commentRepository.update(toModel(comment));
    }

    public void deleteCommentByAdmin(String commentId) {
        CommentDTO comment = getCommentIncludingHidden(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("Comment was not found.");
        }

        commentRepository.delete(commentId);
        deleteCommentLikes(commentId);
    }

    public void deleteCommentsForReview(String reviewId) {
        for (Comment comment : commentRepository.findByReviewId(reviewId)) {
            deleteCommentByAdmin(comment.commentId());
        }
    }

    public boolean isCommentOwner(CommentDTO comment, String currentUserId) {
        return comment != null && Objects.equals(comment.getTouristId(), currentUserId);
    }

    private boolean isVisible(CommentDTO comment) {
        return comment != null && !"HIDDEN".equalsIgnoreCase(comment.getModerationStatus());
    }

    private void deleteCommentLikes(String commentId) {
        commentLikeRepository.findByCommentId(commentId)
                .forEach(like -> commentLikeRepository.delete(like.likeId()));
    }

    private void applyReplyTarget(CommentDTO comment, String requestedReplyId) {
        if (requestedReplyId == null || requestedReplyId.isBlank()) {
            comment.setReplyToCommentId(null);
            comment.setReplyToTouristName(null);
            return;
        }

        CommentDTO replyTarget = toDto(commentRepository.findByCommentId(requestedReplyId));
        if (replyTarget == null
                || !Objects.equals(replyTarget.getReviewId(), comment.getReviewId())
                || !isVisible(replyTarget)) {
            throw new IllegalArgumentException("The comment you are replying to is no longer available.");
        }

        comment.setReplyToCommentId(replyTarget.getCommentId());
        comment.setReplyToTouristName(displayName(replyTarget));
    }

    private String displayName(CommentDTO comment) {
        if (comment == null) {
            return "Visitor";
        }

        String resolvedName = userService.getDisplayNameByUserId(comment.getTouristId());
        if (resolvedName != null && !resolvedName.isBlank()) {
            return resolvedName;
        }

        return comment.getTouristId() == null || comment.getTouristId().isBlank()
                ? "Visitor"
                : comment.getTouristId();
    }

    private String replyTargetName(CommentDTO comment) {
        if (comment == null || comment.getReplyToCommentId() == null
                || comment.getReplyToCommentId().isBlank()) {
            return null;
        }

        CommentDTO replyTarget = toDto(commentRepository.findByCommentId(comment.getReplyToCommentId()));
        return replyTarget == null
                ? comment.getReplyToTouristName()
                : displayName(replyTarget);
    }

    private void enrichDisplayNames(Collection<CommentDTO> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        Map<String, CommentDTO> commentsById = new LinkedHashMap<>();
        for (CommentDTO comment : comments) {
            if (comment == null) {
                continue;
            }
            comment.setTouristName(displayName(comment));
            commentsById.put(comment.getCommentId(), comment);
        }

        for (CommentDTO comment : comments) {
            if (comment == null || comment.getReplyToCommentId() == null
                    || comment.getReplyToCommentId().isBlank()) {
                continue;
            }

            CommentDTO replyTarget = commentsById.get(comment.getReplyToCommentId());
            if (replyTarget != null) {
                comment.setReplyToTouristName(replyTarget.getTouristName());
            } else {
                comment.setReplyToTouristName(replyTargetName(comment));
            }
        }
    }

    private Comment toModel(CommentDTO comment) {
        return new Comment(
                comment.getCommentId(), comment.getReviewId(), comment.getTouristId(), comment.getCommentText(),
                comment.getReplyToCommentId(), comment.getReplyToTouristName(), comment.getCreatedAt(),
                comment.getUpdatedAt(), comment.getModerationStatus());
    }

    private CommentDTO toDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        CommentDTO dto = new CommentDTO();
        dto.setCommentId(comment.commentId());
        dto.setReviewId(comment.reviewId());
        dto.setTouristId(comment.touristId());
        dto.setCommentText(comment.commentText());
        dto.setReplyToCommentId(comment.replyToCommentId());
        dto.setReplyToTouristName(comment.replyToTouristName());
        dto.setCreatedAt(comment.createdAt());
        dto.setUpdatedAt(comment.updatedAt());
        dto.setModerationStatus(comment.moderationStatus());
        return dto;
    }
}
