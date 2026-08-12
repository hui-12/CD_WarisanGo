package com.warisango.model.service;

import com.warisango.dto.ReviewDTO;
import com.warisango.model.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles review business logic before delegating persistence to the repository.
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    /**
     * Display all reviews.
     */
    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAll();
    }

    /**
     * Display reviews by business.
     */
    public List<ReviewDTO> getReviewsByBusiness(String businessId) {
        return reviewRepository.findByBusinessId(businessId);
    }

    /**
     * Display one review.
     */
    public ReviewDTO getReview(String reviewId) {
        return reviewRepository.findByReviewId(reviewId);
    }

    /**
     * Create a review with temporary mock-user data until authentication is connected.
     */
    public void createReview(ReviewDTO review) {

        review.setReviewId(UUID.randomUUID().toString());
        review.setTouristId("MOCK_USER");
        review.setCreatedAt(LocalDateTime.now().toLocalDate().toString());
        review.setUpdatedAt(LocalDateTime.now().toLocalDate().toString());

        reviewRepository.save(review);

    }

    /**
     * Update an existing review without losing immutable mock fields.
     */
    public void updateReview(ReviewDTO review) {

        ReviewDTO existingReview = reviewRepository.findByReviewId(review.getReviewId());

        if (existingReview == null) {
            return;
        }

        review.setBusinessId(existingReview.getBusinessId());
        review.setTouristId(existingReview.getTouristId());
        review.setCreatedAt(existingReview.getCreatedAt());
        review.setUpdatedAt(LocalDateTime.now().toLocalDate().toString());

        reviewRepository.update(review);

    }

    /**
     * Delete one review.
     */
    public void deleteReview(String reviewId) {

        reviewRepository.delete(reviewId);

    }

    /**
     * Count all reviews for one business.
     */
    public int getTotalReviews(String businessId) {

        return reviewRepository.findByBusinessId(businessId).size();

    }

    /**
     * Calculate average rating for one business.
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

        return Math.round(((double) total / reviews.size()) * 10.0) / 10.0;

    }

    /**
     * Build rating distribution rows for the review summary progress bars.
     */
    public List<Map<String, Object>> getRatingDistribution(String businessId) {

        List<ReviewDTO> reviews = reviewRepository.findByBusinessId(businessId);
        List<Map<String, Object>> rows = new ArrayList<>();
        int totalReviews = reviews.size();

        for (int rating = 5; rating >= 1; rating--) {

            int currentRating = rating;
            long count = reviews.stream()
                    .filter(review -> review.getRating() == currentRating)
                    .count();
            int percentage = totalReviews == 0 ? 0 : (int) Math.round((count * 100.0) / totalReviews);

            Map<String, Object> row = new HashMap<>();
            row.put("rating", rating);
            row.put("count", count);
            row.put("percentage", percentage);
            rows.add(row);
        }

        return rows;
    }

    /**
     * Convert the form photo URL textarea into DTO photo URLs.
     */
    public void applyPhotoUrls(ReviewDTO review, String photoUrls) {

        if (photoUrls == null || photoUrls.isBlank()) {
            review.setPhotoUrls(new ArrayList<>());
            return;
        }

        review.setPhotoUrls(photoUrls.lines()
                .map(String::trim)
                .filter(url -> !url.isBlank())
                .collect(Collectors.toList()));
    }

    /**
     * Convert DTO photo URLs into textarea-friendly text.
     */
    public String getPhotoUrlsText(ReviewDTO review) {

        if (review == null || review.getPhotoUrls() == null || review.getPhotoUrls().isEmpty()) {
            return "";
        }

        return String.join(System.lineSeparator(), review.getPhotoUrls());
    }

    public Map<String, Object> getBusinessInformation(String businessId) {

        Map<String, Object> business = new HashMap<>();

        business.put("businessId", businessId);
        business.put("businessName", "Kedai Warisan Laksa");
        business.put("businessAddress", "George Town, Penang, Malaysia");
        business.put("businessImage",
                "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=1400&q=80");
        business.put("category", "Heritage Food");

        if ("BUS001".equals(businessId)) {
            business.put("businessName", "Wulandari Batik Studio");
            business.put("businessAddress", "Laweyan, Solo, Central Java");
            business.put("businessImage",
                    "https://images.unsplash.com/photo-1516550893923-42d28e5677af?auto=format&fit=crop&w=1400&q=80");
            business.put("category", "Heritage Business");
        }

        return business;

    }

}
