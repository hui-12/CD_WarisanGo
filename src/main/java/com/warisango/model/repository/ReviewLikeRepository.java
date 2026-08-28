package com.warisango.model.repository;

import com.warisango.dto.ReviewLikeDTO;

import java.util.List;

/**
 * Repository contract for the root-level reviewLikes collection.
 */
public interface ReviewLikeRepository {

    List<ReviewLikeDTO> findByReviewId(String reviewId);

    void save(ReviewLikeDTO like);

    void delete(String likeId);

    String generateNextLikeId();
}
