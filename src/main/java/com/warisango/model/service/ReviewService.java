package com.warisango.model.service;

import com.warisango.dto.ReviewDTO;
import com.warisango.model.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Handles review business logic before delegating persistence to the repository.
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewPhotoService reviewPhotoService;
    private final String currentTouristId;

    public ReviewService(
            ReviewRepository reviewRepository,
            ReviewPhotoService reviewPhotoService,
            @Value("${warisango.review.current-tourist-id:tourist_002}") String currentTouristId) {
        this.reviewRepository = reviewRepository;
        this.reviewPhotoService = reviewPhotoService;
        this.currentTouristId = currentTouristId;
    }

    /**
     * Display all reviews.
     */
    public List<ReviewDTO> getAllReviews() {
        List<ReviewDTO> reviews = reviewRepository.findAll();
        reviewPhotoService.populatePhotos(reviews);
        return reviews;
    }

    /**
     * Display reviews by business.
     */
    public List<ReviewDTO> getReviewsByBusiness(String businessId) {
        List<ReviewDTO> reviews = reviewRepository.findByBusinessId(businessId);
        reviewPhotoService.populatePhotos(reviews);
        return reviews;
    }

    /**
     * Display one review.
     */
    public ReviewDTO getReview(String reviewId) {
        ReviewDTO review = reviewRepository.findByReviewId(reviewId);
        reviewPhotoService.populatePhotos(review);
        return review;
    }

    /**
     * Create a review without photos. Kept for callers that do not use multipart forms.
     */
    public void createReview(ReviewDTO review) {
        createReview(review, null);
    }

    /**
     * Create a review and persist each uploaded image as a separate ReviewPhotos document.
     */
    public void createReview(ReviewDTO review, MultipartFile[] photos) {

        reviewPhotoService.validateNewPhotos(photos);

        review.setTouristId(currentTouristId);

        String reviewId = reviewRepository.generateNextReviewId();

        review.setReviewId(reviewId);

        review.setCreatedAt(
                LocalDateTime.now()
                        .toLocalDate()
                        .toString()
        );

        review.setUpdatedAt(
                LocalDateTime.now()
                        .toLocalDate()
                        .toString()
        );

        reviewRepository.save(review);

        try {
            reviewPhotoService.savePhotos(review.getReviewId(), photos);
        } catch (RuntimeException e) {
            reviewRepository.delete(review.getReviewId());
            throw e;
        }
    }

    /**
     * Update an existing review without losing immutable mock fields.
     */
    public boolean updateReview(ReviewDTO review, String currentUserId) {
        return updateReview(review, currentUserId, List.of(), null);
    }

    /**
     * Update review fields and apply existing-photo removals/new uploads after ownership validation.
     */
    public boolean updateReview(
            ReviewDTO review,
            String currentUserId,
            List<String> removePhotoIds,
            MultipartFile[] photos) {

        ReviewDTO existingReview =
                reviewRepository.findByReviewId(
                        review.getReviewId()
                );

        if (existingReview == null) {
            return false;
        }

        // Only the review owner can edit.
        if (!Objects.equals(existingReview.getTouristId(), currentUserId)) {
            return false;
        }

        reviewPhotoService.validatePhotoChange(review.getReviewId(), removePhotoIds, photos);

        // Keep immutable fields from the existing review.
        review.setReviewId(existingReview.getReviewId());
        review.setBusinessId(existingReview.getBusinessId());
        review.setTouristId(existingReview.getTouristId());
        review.setCreatedAt(existingReview.getCreatedAt());

        review.setUpdatedAt(
                LocalDateTime.now()
                        .toLocalDate()
                        .toString()
        );

        reviewRepository.update(review);

        reviewPhotoService.replacePhotos(review.getReviewId(), removePhotoIds, photos);

        return true;
    }

    /**
     * Delete one review.
     */
    public boolean deleteReview(
            String reviewId,
            String currentUserId
    ) {

        ReviewDTO existingReview =
                reviewRepository.findByReviewId(reviewId);

        if (existingReview == null) {
            return false;
        }

        // Only the review owner can delete.
        if (!Objects.equals(existingReview.getTouristId(), currentUserId)) {
            return false;
        }

        reviewPhotoService.deletePhotos(reviewId);
        reviewRepository.delete(reviewId);

        return true;
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

    public String getCurrentTouristId() {
        return currentTouristId;
    }

    public boolean isReviewOwner(ReviewDTO review, String currentUserId) {
        return review != null && Objects.equals(review.getTouristId(), currentUserId);
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
