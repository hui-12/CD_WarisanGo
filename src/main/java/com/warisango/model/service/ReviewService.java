package com.warisango.model.service;

import com.warisango.dto.ReviewDTO;
import com.warisango.dto.HeritageBusinessDTO;
import com.warisango.model.repository.ReviewRepository;
import com.warisango.model.repository.ReviewLikeRepository;
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
    private final ContentModerationService contentModerationService;
    private final ReviewLikeRepository reviewLikeRepository;
    private final CommentService commentService;
    private final BusinessService businessService;
    private final UserService userService;
    private final String currentTouristId;

    public ReviewService(
            ReviewRepository reviewRepository,
            ReviewPhotoService reviewPhotoService,
            ContentModerationService contentModerationService,
            ReviewLikeRepository reviewLikeRepository,
            CommentService commentService,
            BusinessService businessService,
            UserService userService,
            @Value("${warisango.review.current-tourist-id:tourist_002}") String currentTouristId) {
        this.reviewRepository = reviewRepository;
        this.reviewPhotoService = reviewPhotoService;
        this.contentModerationService = contentModerationService;
        this.reviewLikeRepository = reviewLikeRepository;
        this.commentService = commentService;
        this.businessService = businessService;
        this.userService = userService;
        this.currentTouristId = currentTouristId;
    }

    /**
     * Display all reviews.
     */
    public List<ReviewDTO> getAllReviews() {
        List<ReviewDTO> reviews = reviewRepository.findAll();
        reviews.removeIf(review -> !isVisible(review));
        reviewPhotoService.populatePhotos(reviews);
        enrichReviewerNames(reviews);
        return reviews;
    }

    /**
     * Display reviews by business.
     */
    public List<ReviewDTO> getReviewsByBusiness(String businessId) {
        List<ReviewDTO> reviews = reviewRepository.findByBusinessId(businessId);
        reviews.removeIf(review -> !isVisible(review));
        reviewPhotoService.populatePhotos(reviews);
        enrichReviewerNames(reviews);
        return reviews;
    }

    /**
     * Display one review.
     */
    public ReviewDTO getReview(String reviewId) {
        ReviewDTO review = reviewRepository.findByReviewId(reviewId);
        if (!isVisible(review)) {
            return null;
        }
        reviewPhotoService.populatePhotos(review);
        enrichReviewerName(review);
        return review;
    }

    /**
     * Loads a review for moderation workflows, including hidden content.
     */
    public ReviewDTO getReviewIncludingHidden(String reviewId) {
        ReviewDTO review = reviewRepository.findByReviewId(reviewId);
        reviewPhotoService.populatePhotos(review);
        enrichReviewerName(review);
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

        contentModerationService.validate(review.getReviewText());
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

        contentModerationService.validate(review.getReviewText());

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
        deleteReviewLikes(reviewId);
        commentService.deleteCommentsForReview(reviewId);
        reviewRepository.delete(reviewId);

        return true;
    }

    public void hideReview(String reviewId) {
        ReviewDTO review = getReviewIncludingHidden(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("Review was not found.");
        }

        review.setModerationStatus("HIDDEN");
        reviewRepository.update(review);
    }

    public void restoreReview(String reviewId) {
        ReviewDTO review = getReviewIncludingHidden(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("Review was not found.");
        }

        review.setModerationStatus("VISIBLE");
        reviewRepository.update(review);
    }

    public void deleteReviewByAdmin(String reviewId) {
        ReviewDTO review = getReviewIncludingHidden(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("Review was not found.");
        }

        reviewPhotoService.deletePhotos(reviewId);
        deleteReviewLikes(reviewId);
        commentService.deleteCommentsForReview(reviewId);
        reviewRepository.delete(reviewId);
    }

    /**
     * Count all reviews for one business.
     */
    public int getTotalReviews(String businessId) {

        return getReviewsByBusiness(businessId).size();

    }

    /**
     * Calculate average rating for one business.
     */
    public double getAverageRating(String businessId) {

        List<ReviewDTO> reviews = getReviewsByBusiness(businessId);

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

        List<ReviewDTO> reviews = getReviewsByBusiness(businessId);
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

    private boolean isVisible(ReviewDTO review) {
        return review != null && !"HIDDEN".equalsIgnoreCase(review.getModerationStatus());
    }

    private void enrichReviewerNames(List<ReviewDTO> reviews) {
        for (ReviewDTO review : reviews) {
            enrichReviewerName(review);
        }
    }

    private void enrichReviewerName(ReviewDTO review) {
        if (review == null || review.getTouristId() == null || review.getTouristId().isBlank()) {
            return;
        }

        review.setTouristName(userService.getDisplayNameByTouristId(review.getTouristId()));
    }

    private void deleteReviewLikes(String reviewId) {
        reviewLikeRepository.findByReviewId(reviewId)
                .forEach(like -> reviewLikeRepository.delete(like.getLikeId()));
    }

    public Map<String, Object> getBusinessInformation(String businessId) {

        Map<String, Object> business = new HashMap<>();

        business.put("businessId", businessId);
        business.put("businessName", "Kedai Warisan Laksa");
        business.put("businessAddress", "George Town, Penang, Malaysia");
        business.put("businessImage",
                "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=1400&q=80");
        business.put("category", "Heritage Food");

        applyFallbackBusinessInformation(business, businessId);

        HeritageBusinessDTO firestoreBusiness = businessService.getApprovedBusinessForReview(businessId);
        if (firestoreBusiness != null) {
            if (hasText(firestoreBusiness.getName())) {
                business.put("businessName", firestoreBusiness.getName());
            }
            if (hasText(firestoreBusiness.getAddress())) {
                business.put("businessAddress", firestoreBusiness.getAddress());
            }
            business.put("category", "Heritage Business");
        }

        return business;

    }

    private void applyFallbackBusinessInformation(Map<String, Object> business, String businessId) {
        if ("hb_001".equals(businessId)) {
            business.put("businessName", "Yut Kee Restaurant");
            business.put("businessAddress", "7, Jalan Kamunting, Kuala Lumpur");
            business.put("businessImage",
                    "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=1400&q=80");
        } else if ("hb_002".equals(businessId)) {
            business.put("businessName", "Restoran Kim Lian Kee");
            business.put("businessAddress", "49, Jalan Petaling, Kuala Lumpur");
            business.put("businessImage",
                    "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=1400&h=900&fit=crop&auto=format");
        } else if ("hb_003".equals(businessId)) {
            business.put("businessName", "Nasi Kandar Line Clear");
            business.put("businessAddress", "177, Jalan Penang, George Town, Penang");
            business.put("businessImage",
                    "https://images.unsplash.com/photo-1516550893923-42d28e5677af?auto=format&fit=crop&w=1400&q=80");
        } else if ("BUS001".equals(businessId)) {
            business.put("businessName", "Wulandari Batik Studio");
            business.put("businessAddress", "Laweyan, Solo, Central Java");
            business.put("businessImage",
                    "https://images.unsplash.com/photo-1516550893923-42d28e5677af?auto=format&fit=crop&w=1400&q=80");
            business.put("category", "Heritage Business");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

}
