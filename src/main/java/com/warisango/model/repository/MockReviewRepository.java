package com.warisango.model.repository;

import com.warisango.dto.ReviewDTO;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MockReviewRepository implements ReviewRepository {

    private final List<ReviewDTO> reviews = new ArrayList<>();

    public MockReviewRepository() {

        List<String> photos = new ArrayList<>();
        photos.add("/images/sample-review1.jpg");
        photos.add("/images/sample-review2.jpg");

        ReviewDTO review = new ReviewDTO();

        review.setReviewId("REV001");
        review.setBusinessId("BUS001");
        review.setTouristId("USR001");
        review.setTouristName("Dewi Anggraini");
        review.setRating(5);
        review.setReviewText(
                "An extraordinary experience! Doing batik hands-on alongside the artisan truly opened my eyes to Indonesia's cultural heritage."
        );
        review.setPhotoUrls(photos);
        review.setCreatedAt(LocalDate.now().minusDays(2).toString());
        review.setUpdatedAt(LocalDate.now().minusDays(2).toString());

        reviews.add(review);
    }

    @Override
    public List<ReviewDTO> findAll() {
        return reviews;
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