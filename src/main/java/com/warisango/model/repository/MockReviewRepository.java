package com.warisango.model.repository;

import com.warisango.dto.ReviewDTO;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides in-memory review data until Firestore review storage is connected.
 */
@Repository
public class MockReviewRepository implements ReviewRepository {

    private final List<ReviewDTO> reviews = new ArrayList<>();

    public MockReviewRepository() {

        reviews.add(createReview(
                "REV001",
                "BUS00",
                "USR001",
                "Aina Rahman",
                5,
                "The laksa was rich and full of old Penang flavor. The owner explained the family recipe, "
                        + "which made the visit feel personal.",
                Arrays.asList(
                        "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?auto=format&fit=crop&w=600&q=80",
                        "https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=600&q=80"
                ),
                2
        ));
        reviews.add(createReview(
                "REV002",
                "BUS00",
                "USR002",
                "Daniel Tan",
                4,
                "Cozy place with friendly staff. The food was excellent, and the old shop interior gives a heritage "
                        + "feeling.",
                List.of("https://images.unsplash.com/photo-1551218808-94e220e084d2?auto=format&fit=crop&w=600&q=80"),
                5
        ));
        reviews.add(createReview(
                "REV003",
                "BUS00",
                "USR003",
                "Nur Iman",
                5,
                "Worth bringing tourists here. The menu is simple, but every dish feels carefully prepared.",
                new ArrayList<>(),
                8
        ));
        reviews.add(createReview(
                "REV004",
                "BUS001",
                "USR004",
                "Dewi Anggraini",
                5,
                "An extraordinary experience. Doing batik with the artisan opened my eyes to cultural heritage.",
                Arrays.asList(
                        "https://images.unsplash.com/photo-1516550893923-42d28e5677af?auto=format&fit=crop&w=600&q=80",
                        "https://images.unsplash.com/photo-1499696010180-025ef6e1a8f9?auto=format&fit=crop&w=600&q=80"
                ),
                3
        ));
    }

    private ReviewDTO createReview(String reviewId,
                                   String businessId,
                                   String touristId,
                                   String touristName,
                                   int rating,
                                   String reviewText,
                                   List<String> photoUrls,
                                   int daysAgo) {

        ReviewDTO review = new ReviewDTO();

        review.setReviewId(reviewId);
        review.setBusinessId(businessId);
        review.setTouristId(touristId);
        review.setTouristName(touristName);
        review.setRating(rating);
        review.setReviewText(reviewText);
        review.setPhotoUrls(photoUrls);
        review.setCreatedAt(LocalDate.now().minusDays(daysAgo).toString());
        review.setUpdatedAt(LocalDate.now().minusDays(daysAgo).toString());

        return review;
    }

    @Override
    public List<ReviewDTO> findAll() {
        return new ArrayList<>(reviews);
    }

    @Override
    public List<ReviewDTO> findByBusinessId(String businessId) {

        List<ReviewDTO> result = new ArrayList<>();

        for (ReviewDTO review : reviews) {

            if (review.getBusinessId().equals(businessId)) {
                result.add(review);
            }

        }

        return result;
    }

    @Override
    public ReviewDTO findByReviewId(String reviewId) {

        for (ReviewDTO review : reviews) {

            if (review.getReviewId().equals(reviewId)) {
                return review;
            }

        }

        return null;
    }

    @Override
    public void save(ReviewDTO review) {

        reviews.add(review);

    }

    @Override
    public void update(ReviewDTO review) {

        for (int i = 0; i < reviews.size(); i++) {

            if (reviews.get(i).getReviewId().equals(review.getReviewId())) {

                reviews.set(i, review);

                return;

            }

        }

    }

    @Override
    public void delete(String reviewId) {

        reviews.removeIf(review -> review.getReviewId().equals(reviewId));

    }

}
