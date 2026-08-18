package com.warisango.model.service;

import com.warisango.dto.CommentDTO;
import com.warisango.dto.CommentLikeDTO;
import com.warisango.dto.LikeStatusDTO;
import com.warisango.model.repository.CommentLikeRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Handles CommentLikes toggle rules and display state.
 */
@Service
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;

    public CommentLikeService(CommentLikeRepository commentLikeRepository) {
        this.commentLikeRepository = commentLikeRepository;
    }

    public LikeStatusDTO getStatus(String commentId, String touristId) {
        List<CommentLikeDTO> likes = commentLikeRepository.findByCommentId(commentId);
        boolean liked = likes.stream().anyMatch(like -> Objects.equals(like.getTouristId(), touristId));
        return new LikeStatusDTO(liked, likes.size());
    }

    public LikeStatusDTO toggleLike(String commentId, String touristId) {
        List<CommentLikeDTO> likes = commentLikeRepository.findByCommentId(commentId);
        CommentLikeDTO existingLike = likes.stream()
                .filter(like -> Objects.equals(like.getTouristId(), touristId))
                .findFirst()
                .orElse(null);

        if (existingLike != null) {
            likes.stream()
                    .filter(like -> Objects.equals(like.getTouristId(), touristId))
                    .forEach(like -> commentLikeRepository.delete(like.getLikeId()));
        } else {
            commentLikeRepository.save(new CommentLikeDTO(
                    commentLikeRepository.generateNextLikeId(),
                    commentId,
                    touristId,
                    null
            ));
        }

        return getStatus(commentId, touristId);
    }

    public void enrichComment(CommentDTO comment, String touristId) {
        if (comment == null || comment.getCommentId() == null || comment.getCommentId().isBlank()) {
            return;
        }

        LikeStatusDTO status = getStatus(comment.getCommentId(), touristId);
        comment.setLikeCount(status.likeCount());
        comment.setLikedByCurrentUser(status.liked());
    }

    public void enrichComments(Collection<CommentDTO> comments, String touristId) {
        if (comments != null) {
            comments.forEach(comment -> enrichComment(comment, touristId));
        }
    }

    public void enrichCommentMap(Map<String, List<CommentDTO>> commentsByReview, String touristId) {
        if (commentsByReview != null) {
            commentsByReview.values().forEach(comments -> enrichComments(comments, touristId));
        }
    }
}
