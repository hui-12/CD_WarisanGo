package com.warisango.model.service;

import com.warisango.dto.ReviewDTO;
import com.warisango.model.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    /*
     * Display all reviews
     */
    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAll();
    }

    /*
     * Display reviews by business
     */
    public List<ReviewDTO> getReviewsByBusiness(String businessId) {
        return reviewRepository.findByBusinessId(businessId);
    }

    /*
     * Display one review
     */
    public ReviewDTO getReview(String reviewId) {
        return reviewRepository.findByReviewId(reviewId);
    }

    /*
     * Create Review
     */
    public void createReview(ReviewDTO review) {

        review.setReviewId(UUID.randomUUID().toString());

        review.setCreatedAt(LocalDateTime.now().toString());

        review.setUpdatedAt(LocalDateTime.now().toString());

        reviewRepository.save(review);

    }

    /*
     * Update Review
     */
    public void updateReview(ReviewDTO review) {

        review.setUpdatedAt(LocalDateTime.now().toString());

        reviewRepository.update(review);

    }

    /*
     * Delete Review
     */
    public void deleteReview(String reviewId) {

        reviewRepository.delete(reviewId);

    }

    /*
     * Total Reviews
     */
    public int getTotalReviews(String businessId) {

        return reviewRepository.findByBusinessId(businessId).size();

    }

    /*
     * Average Rating
     */
    public double getAverageRating(String businessId) {

        List<ReviewDTO> reviews =
                reviewRepository.findByBusinessId(businessId);

        if (reviews.isEmpty()) {
            return 0;
        }

        int total = 0;

        for (ReviewDTO review : reviews) {

            total += review.getRating();

        }

        return (double) total / reviews.size();

    }
    public Map<String, Object> getBusinessInformation(String businessId) {

        Map<String, Object> business = new HashMap<>();

        business.put("businessId", businessId);
        business.put("businessName", "Wulandari Batik Studio");
        business.put("businessAddress", "Laweyan, Solo, Central Java");
        business.put("businessImage", "https://picsum.photos/800/500");
        business.put("category", "Heritage Business");

        return business;

    }

}