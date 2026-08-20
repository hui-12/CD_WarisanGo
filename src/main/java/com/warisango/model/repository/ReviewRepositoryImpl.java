package com.warisango.model.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.warisango.dto.ReviewDTO;
import com.warisango.util.ReviewDateFormatter;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ReviewRepositoryImpl implements ReviewRepository {

    private static final String COLLECTION = "Reviews";

    private final Firestore firestore;

    public ReviewRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }


    // =====================================================
    // FIND ALL REVIEWS
    // =====================================================

    @Override
    public List<ReviewDTO> findAll() {

        List<ReviewDTO> reviews = new ArrayList<>();

        try {

            ApiFuture<QuerySnapshot> future =
                    firestore
                            .collection(COLLECTION)
                            .get();

            List<QueryDocumentSnapshot> documents =
                    future.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {

                ReviewDTO review =
                        convertDocumentToReview(document);

                if (review != null) {
                    reviews.add(review);
                }
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to load reviews from Firestore.",
                    e
            );
        }

        return reviews;
    }


    // =====================================================
    // FIND REVIEWS BY BUSINESS ID
    // =====================================================

    @Override
    public List<ReviewDTO> findByBusinessId(
            String businessId
    ) {

        List<ReviewDTO> reviews = new ArrayList<>();

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

                ReviewDTO review =
                        convertDocumentToReview(document);

                if (review != null) {
                    reviews.add(review);
                }
            }

        } catch (Exception e) {

            throw new RuntimeException(
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

    @Override
    public ReviewDTO findByReviewId(
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

            throw new RuntimeException(
                    "Failed to load review: "
                            + reviewId,
                    e
            );
        }
    }


    // =====================================================
    // SAVE REVIEW
    // =====================================================

    @Override
    public void save(
            ReviewDTO review
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

            throw new RuntimeException(
                    "Failed to save review: "
                            + review.getReviewId(),
                    e
            );
        }
    }


    // =====================================================
    // UPDATE REVIEW
    // =====================================================

    @Override
    public void update(
            ReviewDTO review
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

            throw new RuntimeException(
                    "Failed to update review: "
                            + review.getReviewId(),
                    e
            );
        }
    }


    // =====================================================
    // DELETE REVIEW
    // =====================================================

    @Override
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

            throw new RuntimeException(
                    "Failed to delete review: "
                            + reviewId,
                    e
            );
        }
    }


    // =====================================================
    // FIRESTORE -> REVIEW DTO
    // =====================================================

    private ReviewDTO convertDocumentToReview(
            DocumentSnapshot document
    ) {

        ReviewDTO review = new ReviewDTO();


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
         * Your Reviews collection does not have
         * touristName.
         *
         * We will connect this to Tourists/Users
         * later.
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
         * ReviewPhotoService enriches this DTO from the root-level ReviewPhotos collection.
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
            ReviewDTO review
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
            ReviewDTO review
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

    @Override
    public String generateNextReviewId() {

        try {

            List<QueryDocumentSnapshot> documents =
                    firestore
                            .collection(COLLECTION)
                            .get()
                            .get()
                            .getDocuments();

            int maxNumber = 0;

            for (QueryDocumentSnapshot document : documents) {

                String reviewId =
                        document.getString("reviewId");

                if (reviewId == null) {
                    continue;
                }

                if (!reviewId.startsWith("review_")) {
                    continue;
                }

                String numberPart =
                        reviewId.substring("review_".length());

                try {

                    int number =
                            Integer.parseInt(numberPart);

                    if (number > maxNumber) {
                        maxNumber = number;
                    }

                } catch (NumberFormatException ignored) {
                    // Ignore IDs that do not follow review_001 format.
                }
            }

            return String.format(
                    "review_%03d",
                    maxNumber + 1
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate next review ID.",
                    e
            );
        }
    }
}
