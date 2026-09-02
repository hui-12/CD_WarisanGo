package com.warisango.model.repository;

import com.warisango.dto.ReviewPhotoDTO;

import java.util.List;

public interface ReviewPhotoRepository {

    List<ReviewPhotoDTO> findAll();

    List<ReviewPhotoDTO> findByReviewId(String reviewId);

    void save(ReviewPhotoDTO photo);

    void delete(String photoId);

    String generateNextPhotoId();
}
