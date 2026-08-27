package com.warisango.model.repository;

import com.warisango.dto.CommentLikeDTO;

import java.util.List;

/**
 * Repository contract for the root-level commentLikes collection.
 */
public interface CommentLikeRepository {

    List<CommentLikeDTO> findByCommentId(String commentId);

    void save(CommentLikeDTO like);

    void delete(String likeId);

    String generateNextLikeId();
}
