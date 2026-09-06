package com.warisango.service;

import com.warisango.dto.LikeStatusDTO;
import com.warisango.dto.ReviewDTO;
import com.warisango.model.ReviewLike;
import com.warisango.repository.ReviewLikeRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Handles reviewLikes toggle rules and display state.
 */
@Service
public class ReviewLikeService {

    private final ReviewLikeRepository reviewLikeRepository;

    public ReviewLikeService(ReviewLikeRepository reviewLikeRepository) {
        this.reviewLikeRepository = reviewLikeRepository;
    }

    public LikeStatusDTO getStatus(String reviewId, String touristId) {
        List<ReviewLike> likes = reviewLikeRepository.findByReviewId(reviewId);
        boolean liked = likes.stream().anyMatch(like -> Objects.equals(like.touristId(), touristId));
        return new LikeStatusDTO(liked, likes.size());
    }

    public LikeStatusDTO toggleLike(String reviewId, String touristId) {
        List<ReviewLike> likes = reviewLikeRepository.findByReviewId(reviewId);
        ReviewLike existingLike = likes.stream()
                .filter(like -> Objects.equals(like.touristId(), touristId))
                .findFirst()
                .orElse(null);

        if (existingLike != null) {
            likes.stream()
                    .filter(like -> Objects.equals(like.touristId(), touristId))
                    .forEach(like -> reviewLikeRepository.delete(like.likeId()));
        } else {
            reviewLikeRepository.save(new ReviewLike(
                    reviewLikeRepository.generateNextLikeId(),
                    reviewId,
                    touristId,
                    null
            ));
        }

        return getStatus(reviewId, touristId);
    }

    public void enrichReview(ReviewDTO review, String touristId) {
        if (review == null || review.getReviewId() == null || review.getReviewId().isBlank()) {
            return;
        }

        LikeStatusDTO status = getStatus(review.getReviewId(), touristId);
        review.setLikeCount(status.likeCount());
        review.setLikedByCurrentUser(status.liked());
    }

    public void enrichReviews(Collection<ReviewDTO> reviews, String touristId) {
        if (reviews != null) {
            reviews.forEach(review -> enrichReview(review, touristId));
        }
    }
}
