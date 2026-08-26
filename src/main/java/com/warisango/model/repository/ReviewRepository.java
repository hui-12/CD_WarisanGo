package com.warisango.model.repository;

import com.warisango.dto.ReviewDTO;

import java.util.List;

public interface ReviewRepository {

    List<ReviewDTO> findAll();

    List<ReviewDTO> findByBusinessId(String businessId);

    ReviewDTO findByReviewId(String reviewId);

    void save(ReviewDTO review);

    void update(ReviewDTO review);

    void delete(String reviewId);

    String generateNextReviewId();
}