package com.warisango.controller;

import com.warisango.dto.CommentDTO;
import com.warisango.dto.LikeStatusDTO;
import com.warisango.dto.ReviewDTO;
import com.warisango.model.service.CommentService;
import com.warisango.model.service.CommentLikeService;
import com.warisango.model.service.ReviewService;
import com.warisango.model.service.ReviewLikeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * Handles Review and Rating page requests for heritage businesses.
 */
@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewService reviewService;
    private final CommentService commentService;
    private final ReviewLikeService reviewLikeService;
    private final CommentLikeService commentLikeService;

    public ReviewController(
            ReviewService reviewService,
            CommentService commentService,
            ReviewLikeService reviewLikeService,
            CommentLikeService commentLikeService) {
        this.reviewService = reviewService;
        this.commentService = commentService;
        this.reviewLikeService = reviewLikeService;
        this.commentLikeService = commentLikeService;
    }

    /**
     * Prevent multipart uploads and server-managed fields from being bound into ReviewDTO.
     * Uploaded files are received separately through the MultipartFile[] parameter.
     */
    @InitBinder("review")
    public void configureReviewBinder(WebDataBinder binder) {
        binder.setDisallowedFields(
                "photos",
                "photoUrls",
                "touristId",
                "createdAt",
                "updatedAt"
        );
    }

    @InitBinder({"comment", "commentEdit"})
    public void configureCommentBinder(WebDataBinder binder) {
        binder.setDisallowedFields(
                "touristId",
                "touristName",
                "createdAt",
                "updatedAt"
        );
    }

    @GetMapping("/{businessId}")
    public String reviewPage(@PathVariable String businessId, Model model) {
        double averageRating = reviewService.getAverageRating(businessId);
        List<ReviewDTO> reviews = reviewService.getReviewsByBusiness(businessId);
        String currentUserId = reviewService.getCurrentTouristId();
        reviewLikeService.enrichReviews(reviews, currentUserId);
        Map<String, List<CommentDTO>> commentsByReview = commentService.getCommentPreviews(reviews);
        commentLikeService.enrichCommentMap(commentsByReview, currentUserId);

        model.addAttribute("business", reviewService.getBusinessInformation(businessId));
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("averageRatingRounded", Math.round(averageRating));
        model.addAttribute("totalReviews", reviewService.getTotalReviews(businessId));
        model.addAttribute("ratingRows", reviewService.getRatingDistribution(businessId));
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("commentsByReview", commentsByReview);

        return "ReviewAndRatingPage";
    }

    @GetMapping("/create/{businessId}")
    public String createReviewPage(@PathVariable String businessId, Model model) {
        ReviewDTO review = new ReviewDTO();
        review.setBusinessId(businessId);

        model.addAttribute("review", review);
        model.addAttribute("business", reviewService.getBusinessInformation(businessId));

        return "ReviewFormPage";
    }

    @PostMapping("/create")
    public String createReview(
            @Valid @ModelAttribute("review") ReviewDTO review,
            BindingResult bindingResult,
            @RequestParam(name = "photos", required = false) MultipartFile[] photos,
            Model model) {

        logger.info("POST /reviews/create businessId={}, photoParts={}",
                review.getBusinessId(), photos == null ? 0 : photos.length);

        if (bindingResult.hasErrors()) {
            logger.warn("Review create validation errors: {}", bindingResult.getAllErrors());
            addBusiness(model, review.getBusinessId());
            model.addAttribute("reviewError", "Please correct the highlighted fields.");
            return "ReviewFormPage";
        }

        try {
            reviewService.createReview(review, photos);
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("Review create was rejected: {}", e.getMessage(), e);
            addBusiness(model, review.getBusinessId());
            String message = e.getMessage() == null
                    ? "The uploaded photo could not be processed."
                    : e.getMessage();
            model.addAttribute("photoError", message);
            return "ReviewFormPage";
        } catch (RuntimeException e) {
            logger.error("Could not create review for business {}", review.getBusinessId(), e);
            addBusiness(model, review.getBusinessId());
            model.addAttribute("reviewError", "The review could not be saved. Please try again.");
            return "ReviewFormPage";
        }

        return "redirect:/reviews/" + review.getBusinessId();
    }

    @GetMapping("/edit/{reviewId}")
    public String editReviewPage(@PathVariable String reviewId, Model model) {
        ReviewDTO review = reviewService.getReview(reviewId);

        if (review == null) {
            return "redirect:/reviews/BUS00";
        }

        if (!reviewService.isReviewOwner(review, reviewService.getCurrentTouristId())) {
            return "redirect:/reviews/detail/" + reviewId;
        }

        model.addAttribute("review", review);
        model.addAttribute("business", reviewService.getBusinessInformation(review.getBusinessId()));

        return "ReviewEditPage";
    }

    @PostMapping("/update")
    public String updateReview(
            @Valid @ModelAttribute("review") ReviewDTO review,
            BindingResult bindingResult,
            @RequestParam(name = "removePhotoIds", required = false) List<String> removePhotoIds,
            @RequestParam(name = "photos", required = false) MultipartFile[] photos,
            Model model) {

        String currentUserId = reviewService.getCurrentTouristId();
        ReviewDTO existingReview = reviewService.getReview(review.getReviewId());

        logger.info("POST /reviews/update reviewId={}, photoParts={}, removePhotoParts={}",
                review.getReviewId(),
                photos == null ? 0 : photos.length,
                removePhotoIds == null ? 0 : removePhotoIds.size());

        if (existingReview == null) {
            return "redirect:/reviews/BUS00";
        }

        if (!reviewService.isReviewOwner(existingReview, currentUserId)) {
            return "redirect:/reviews/detail/" + existingReview.getReviewId();
        }

        if (bindingResult.hasErrors()) {
            logger.warn("Review update validation errors: {}", bindingResult.getAllErrors());
            restoreExistingPhotos(review, existingReview);
            addBusiness(model, existingReview.getBusinessId());
            model.addAttribute("reviewError", "Please correct the highlighted fields.");
            return "ReviewEditPage";
        }

        try {
            boolean updated = reviewService.updateReview(
                    review,
                    currentUserId,
                    removePhotoIds,
                    photos
            );

            if (!updated) {
                return "redirect:/reviews/" + existingReview.getBusinessId();
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("Review update was rejected: {}", e.getMessage(), e);
            restoreExistingPhotos(review, existingReview);
            addBusiness(model, existingReview.getBusinessId());
            String message = e.getMessage() == null
                    ? "The uploaded photo could not be processed."
                    : e.getMessage();
            model.addAttribute("photoError", message);
            return "ReviewEditPage";
        } catch (RuntimeException e) {
            logger.error("Could not update review {}", existingReview.getReviewId(), e);
            restoreExistingPhotos(review, existingReview);
            addBusiness(model, existingReview.getBusinessId());
            model.addAttribute("reviewError", "The review could not be updated. Please try again.");
            return "ReviewEditPage";
        }

        return "redirect:/reviews/" + existingReview.getBusinessId();
    }

    @GetMapping("/detail/{reviewId}")
    public String reviewDetailPage(
            @PathVariable String reviewId,
            @RequestParam(name = "editComment", required = false) String editCommentId,
            Model model) {
        ReviewDTO review = reviewService.getReview(reviewId);

        if (review == null) {
            return "redirect:/reviews/BUS00";
        }

        List<CommentDTO> comments = commentService.getCommentsByReview(reviewId);
        String currentUserId = reviewService.getCurrentTouristId();
        reviewLikeService.enrichReview(review, currentUserId);
        commentLikeService.enrichComments(comments, currentUserId);
        CommentDTO comment = new CommentDTO();
        comment.setReviewId(reviewId);

        CommentDTO commentEdit = new CommentDTO();
        commentEdit.setReviewId(reviewId);
        String editingCommentId = null;

        if (editCommentId != null && !editCommentId.isBlank()) {
            CommentDTO requestedComment = commentService.getComment(editCommentId);

            if (requestedComment != null
                    && reviewId.equals(requestedComment.getReviewId())
                    && commentService.isCommentOwner(
                    requestedComment,
                    currentUserId)) {
                commentEdit = requestedComment;
                editingCommentId = requestedComment.getCommentId();
            }
        }

        addReviewDetailModel(
                model,
                review,
                comments,
                comment,
                commentEdit,
                editingCommentId,
                null
        );

        return "ReviewDetailPage";
    }

    @PostMapping("/like/{reviewId}")
    @ResponseBody
    public ResponseEntity<LikeStatusDTO> toggleReviewLike(@PathVariable String reviewId) {
        if (reviewService.getReview(reviewId) == null) {
            return ResponseEntity.notFound().build();
        }

        LikeStatusDTO status = reviewLikeService.toggleLike(
                reviewId,
                reviewService.getCurrentTouristId()
        );
        return ResponseEntity.ok(status);
    }

    @PostMapping("/comments/like/{commentId}")
    @ResponseBody
    public ResponseEntity<LikeStatusDTO> toggleCommentLike(@PathVariable String commentId) {
        if (commentService.getComment(commentId) == null) {
            return ResponseEntity.notFound().build();
        }

        LikeStatusDTO status = commentLikeService.toggleLike(
                commentId,
                reviewService.getCurrentTouristId()
        );
        return ResponseEntity.ok(status);
    }

    @PostMapping("/comments/create")
    public String createComment(
            @Valid @ModelAttribute("comment") CommentDTO comment,
            BindingResult bindingResult,
            Model model) {

        String reviewId = comment.getReviewId();

        if (reviewId == null || reviewId.isBlank()) {
            return "redirect:/reviews/BUS00";
        }

        ReviewDTO review = reviewService.getReview(reviewId);

        if (review == null) {
            return "redirect:/reviews/BUS00";
        }

        if (bindingResult.hasErrors()) {
            addReviewDetailModel(
                    model,
                    review,
                    commentService.getCommentsByReview(reviewId),
                    comment,
                    emptyCommentEdit(reviewId),
                    null,
                    "Please correct the comment before submitting."
            );
            return "ReviewDetailPage";
        }

        try {
            commentService.createComment(comment, reviewService.getCurrentTouristId());
        } catch (RuntimeException e) {
            logger.error("Could not create comment for review {}", reviewId, e);
            addReviewDetailModel(
                    model,
                    review,
                    commentService.getCommentsByReview(reviewId),
                    comment,
                    emptyCommentEdit(reviewId),
                    null,
                    "The comment could not be saved. Please try again."
            );
            return "ReviewDetailPage";
        }

        return "redirect:/reviews/detail/" + reviewId;
    }

    @PostMapping("/comments/update")
    public String updateComment(
            @Valid @ModelAttribute("commentEdit") CommentDTO commentEdit,
            BindingResult bindingResult,
            Model model) {

        if (commentEdit == null
                || commentEdit.getCommentId() == null
                || commentEdit.getCommentId().isBlank()) {
            return "redirect:/reviews/BUS00";
        }

        CommentDTO existingComment = commentService.getComment(commentEdit.getCommentId());

        if (existingComment == null) {
            return "redirect:/reviews/BUS00";
        }

        String reviewId = existingComment.getReviewId();
        String currentUserId = reviewService.getCurrentTouristId();

        if (!commentService.isCommentOwner(existingComment, currentUserId)) {
            return "redirect:/reviews/detail/" + reviewId;
        }

        ReviewDTO review = reviewService.getReview(reviewId);

        if (review == null) {
            return "redirect:/reviews/BUS00";
        }

        if (bindingResult.hasErrors()) {
            addReviewDetailModel(
                    model,
                    review,
                    commentService.getCommentsByReview(reviewId),
                    newCommentForReview(reviewId),
                    commentEdit,
                    existingComment.getCommentId(),
                    "Please correct the comment before saving."
            );
            return "ReviewDetailPage";
        }

        try {
            boolean updated = commentService.updateComment(commentEdit, currentUserId);

            if (!updated) {
                return "redirect:/reviews/detail/" + reviewId;
            }
        } catch (RuntimeException e) {
            logger.error("Could not update comment {}", existingComment.getCommentId(), e);
            addReviewDetailModel(
                    model,
                    review,
                    commentService.getCommentsByReview(reviewId),
                    newCommentForReview(reviewId),
                    commentEdit,
                    existingComment.getCommentId(),
                    "The comment could not be updated. Please try again."
            );
            return "ReviewDetailPage";
        }

        return "redirect:/reviews/detail/" + reviewId;
    }

    @PostMapping("/comments/delete/{commentId}")
    public String deleteComment(
            @PathVariable String commentId,
            @RequestParam(name = "reviewId", required = false) String requestedReviewId) {

        CommentDTO existingComment = commentService.getComment(commentId);
        String reviewId = existingComment == null
                ? requestedReviewId
                : existingComment.getReviewId();

        if (existingComment != null) {
            commentService.deleteComment(commentId, reviewService.getCurrentTouristId());
        }

        return reviewId == null || reviewId.isBlank()
                ? "redirect:/reviews/BUS00"
                : "redirect:/reviews/detail/" + reviewId;
    }

    @PostMapping("/delete/{reviewId}")
    public String deleteReview(
            @PathVariable String reviewId,
            @RequestParam(name = "businessId", required = false) String requestedBusinessId) {

        ReviewDTO existingReview = reviewService.getReview(reviewId);
        String businessId = existingReview == null
                ? requestedBusinessId
                : existingReview.getBusinessId();

        if (existingReview != null) {
            reviewService.deleteReview(reviewId, reviewService.getCurrentTouristId());
        }

        return "redirect:/reviews/" + (businessId == null || businessId.isBlank() ? "BUS00" : businessId);
    }

    private void addBusiness(Model model, String businessId) {
        model.addAttribute("business", reviewService.getBusinessInformation(businessId));
    }

    private void restoreExistingPhotos(ReviewDTO review, ReviewDTO existingReview) {
        review.setPhotos(existingReview.getPhotos());
        review.setPhotoUrls(existingReview.getPhotoUrls());
    }

    private void addReviewDetailModel(
            Model model,
            ReviewDTO review,
            List<CommentDTO> comments,
            CommentDTO comment,
            CommentDTO commentEdit,
            String editingCommentId,
            String commentError) {

        model.addAttribute("review", review);
        model.addAttribute("business", reviewService.getBusinessInformation(review.getBusinessId()));
        model.addAttribute("comments", comments);
        model.addAttribute("comment", comment);
        model.addAttribute("commentEdit", commentEdit);
        model.addAttribute("editingCommentId", editingCommentId);
        model.addAttribute("currentUserId", reviewService.getCurrentTouristId());

        if (commentError != null) {
            model.addAttribute("commentError", commentError);
        }
    }

    private CommentDTO emptyCommentEdit(String reviewId) {
        return newCommentForReview(reviewId);
    }

    private CommentDTO newCommentForReview(String reviewId) {
        CommentDTO comment = new CommentDTO();
        comment.setReviewId(reviewId);
        return comment;
    }
}
