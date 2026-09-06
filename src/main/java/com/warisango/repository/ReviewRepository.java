package com.warisango.repository;

import com.warisango.exception.FirebasePersistenceException;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.warisango.model.Review;
import com.warisango.util.ReviewDateFormatter;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ReviewRepository {

    private static final String COLLECTION = "reviews";

    private final Firestore firestore;

    public ReviewRepository(Firestore firestore) {
        this.firestore = firestore;
    }


    // =====================================================
    // FIND ALL REVIEWS
    // =====================================================

    public List<Review> findAll() {

        List<Review> reviews = new ArrayList<>();

        try {

            ApiFuture<QuerySnapshot> future =
                    firestore
                            .collection(COLLECTION)
                            .get();

            List<QueryDocumentSnapshot> documents =
                    future.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {

                Review review =
                        convertDocumentToReview(document);

                if (review != null) {
                    reviews.add(review);
                }
            }

        } catch (Exception e) {

            throw new FirebasePersistenceException(
                    "Failed to load reviews from Firestore.",
                    e
            );
        }

        return reviews;
    }


    // =====================================================
    // FIND REVIEWS BY BUSINESS ID
    // =====================================================

    public List<Review> findByBusinessId(
            String businessId
    ) {

        List<Review> reviews = new ArrayList<>();

        try {

            ApiFuture<QuerySnapshot> future =
                    firestore
                            .collection(COLLECTION)
                            .whereEqualTo(
                                    "businessId",
                                    businessId
                            )
                            .get();

            List<QueryDocumentSnapshot> documents =
                    future.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {

                Review review =
                        convertDocumentToReview(document);

                if (review != null) {
                    reviews.add(review);
                }
            }

        } catch (Exception e) {

            throw new FirebasePersistenceException(
                    "Failed to load reviews for business: "
                            + businessId,
                    e
            );
        }

        return reviews;
    }


    // =====================================================
    // FIND ONE REVIEW
    // =====================================================

    public Review findByReviewId(
            String reviewId
    ) {

        try {

            DocumentSnapshot document =
                    firestore
                            .collection(COLLECTION)
                            .document(reviewId)
                            .get()
                            .get();

            if (!document.exists()) {
                return null;
            }

            return convertDocumentToReview(document);

        } catch (Exception e) {

            throw new FirebasePersistenceException(
                    "Failed to load review: "
                            + reviewId,
                    e
            );
        }
    }


    // =====================================================
    // SAVE REVIEW
    // =====================================================

    public void save(
            Review review
    ) {

        try {

            if (review.getReviewId() == null ||
                    review.getReviewId().isBlank()) {

                throw new IllegalArgumentException(
                        "Review ID cannot be empty."
                );
            }

            DocumentReference document =
                    firestore
                            .collection(COLLECTION)
                            .document(review.getReviewId());

            document
                    .set(convertReviewToDocument(review))
                    .get();

        } catch (Exception e) {

            throw new FirebasePersistenceException(
                    "Failed to save review: "
                            + review.getReviewId(),
                    e
            );
        }
    }


    // =====================================================
    // UPDATE REVIEW
    // =====================================================

    public void update(
            Review review
    ) {

        try {

            if (review.getReviewId() == null ||
                    review.getReviewId().isBlank()) {

                throw new IllegalArgumentException(
                        "Review ID cannot be empty."
                );
            }

            DocumentReference document =
                    firestore
                            .collection(COLLECTION)
                            .document(review.getReviewId());

            document
                    .update(convertReviewToUpdateDocument(review))
                    .get();

        } catch (Exception e) {

            throw new FirebasePersistenceException(
                    "Failed to update review: "
                            + review.getReviewId(),
                    e
            );
        }
    }


    // =====================================================
    // DELETE REVIEW
    // =====================================================

    public void delete(
            String reviewId
    ) {

        try {

            firestore
                    .collection(COLLECTION)
                    .document(reviewId)
                    .delete()
                    .get();

        } catch (Exception e) {

            throw new FirebasePersistenceException(
                    "Failed to delete review: "
                            + reviewId,
                    e
            );
        }
    }


    // =====================================================
    // FIRESTORE -> REVIEW DTO
    // =====================================================

    private Review convertDocumentToReview(
            DocumentSnapshot document
    ) {

        Review review = new Review();


        // reviewId
        review.setReviewId(
                getString(
                        document,
                        "reviewId"
                )
        );


        // businessId
        review.setBusinessId(
                getString(
                        document,
                        "businessId"
                )
        );


        // touristId
        review.setTouristId(
                getString(
                        document,
                        "touristId"
                )
        );


        // reviewText
        review.setReviewText(
                getString(
                        document,
                        "reviewText"
                )
        );


        // rating
        Object ratingValue = document.get("rating");

        if (ratingValue instanceof Integer) {

            review.setRating((Integer) ratingValue);

        } else if (ratingValue instanceof Long) {

            review.setRating(((Long) ratingValue).intValue());

        } else if (ratingValue instanceof Double) {

            review.setRating(((Double) ratingValue).intValue());

        } else if (ratingValue != null) {

            try {
                review.setRating(
                        Integer.parseInt(
                                ratingValue.toString()
                        )
                );
            } catch (NumberFormatException e) {
                review.setRating(0);
            }

        }


        /*
         * The reviews collection does not have
         * touristName.
         *
         * ReviewService resolves this Firebase user UID through the users
         * collection before the review is rendered.
         */
        review.setTouristName(
                getString(
                        document,
                        "touristId"
                )
        );

        review.setModerationStatus(
                getString(document, "moderationStatus")
        );


        /*
         * ReviewPhotoService enriches this DTO from the root-level reviewPhotos collection.
         */
        review.setPhotoUrls(
                new ArrayList<>()
        );


        // createdAt
        Object createdAt = document.get("createdAt");

        if (createdAt != null) {
            review.setCreatedAt(ReviewDateFormatter.format(createdAt));
        }


        // updatedAt
        Object updatedAt = document.get("updatedAt");

        if (updatedAt != null) {
            review.setUpdatedAt(ReviewDateFormatter.format(updatedAt));
        }


        return review;
    }


    // =====================================================
    // REVIEW DTO -> FIRESTORE
    // =====================================================

    private Map<String, Object> convertReviewToDocument(
            Review review
    ) {

        Map<String, Object> data =
                new HashMap<>();


        data.put(
                "reviewId",
                review.getReviewId()
        );

        data.put(
                "touristId",
                review.getTouristId()
        );

        data.put(
                "businessId",
                review.getBusinessId()
        );

        data.put(
                "rating",
                review.getRating()
        );

        data.put(
                "reviewText",
                review.getReviewText()
        );

        data.put(
                "moderationStatus",
                review.getModerationStatus()
        );


        /*
         * Use Firestore server timestamp.
         */
        data.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        data.put(
                "updatedAt",
                FieldValue.serverTimestamp()
        );


        return data;
    }

    /**
     * Updates only mutable review fields so the original createdAt timestamp is preserved.
     */
    private Map<String, Object> convertReviewToUpdateDocument(
            Review review
    ) {
        Map<String, Object> data = new HashMap<>();

        data.put("reviewId", review.getReviewId());
        data.put("touristId", review.getTouristId());
        data.put("businessId", review.getBusinessId());
        data.put("rating", review.getRating());
        data.put("reviewText", review.getReviewText());
        data.put("moderationStatus", review.getModerationStatus());
        data.put("updatedAt", FieldValue.serverTimestamp());

        return data;
    }


    // =====================================================
    // SAFE STRING READER
    // =====================================================

    private String getString(
            DocumentSnapshot document,
            String field
    ) {

        String value =
                document.getString(field);

        return value == null
                ? ""
                : value;
    }

    public String generateNextReviewId() {
        return firestore.collection(COLLECTION).document().getId();
    }

    public int countByTouristId(String touristId) {
        try {
            return firestore.collection(COLLECTION)
                    .whereEqualTo("touristId", touristId)
                    .get()
                    .get()
                    .size();
        } catch (Exception exception) {
            throw new FirebasePersistenceException("Failed to count reviews for tourist: " + touristId, exception);
        }
    }
}
