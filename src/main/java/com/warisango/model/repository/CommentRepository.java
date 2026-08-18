package com.warisango.model.repository;

import com.warisango.dto.CommentDTO;

import java.util.List;

/**
 * Repository contract for the root-level Comments collection.
 */
public interface CommentRepository {

    List<CommentDTO> findByReviewId(String reviewId);

    CommentDTO findByCommentId(String commentId);

    void save(CommentDTO comment);

    void update(CommentDTO comment);

    void delete(String commentId);

    String generateNextCommentId();
}
