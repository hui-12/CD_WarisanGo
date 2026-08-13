package com.warisango.controller;

import com.warisango.dto.ReviewDTO;
import com.warisango.model.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Handles Review and Rating page requests for heritage businesses.
 */
@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/{businessId}")
    public String reviewPage(@PathVariable String businessId,
                             Model model) {

        double averageRating = reviewService.getAverageRating(businessId);

        model.addAttribute("business",
                reviewService.getBusinessInformation(businessId));

        model.addAttribute("reviews",
                reviewService.getReviewsByBusiness(businessId));

        model.addAttribute("averageRating", averageRating);

        model.addAttribute("averageRatingRounded",
                Math.round(averageRating));

        model.addAttribute("totalReviews",
                reviewService.getTotalReviews(businessId));

        model.addAttribute("ratingRows",
                reviewService.getRatingDistribution(businessId));

        return "ReviewAndRatingPage";
    }

    @GetMapping("/create/{businessId}")
    public String createReviewPage(@PathVariable String businessId,
                                   Model model) {

        ReviewDTO review = new ReviewDTO();

        review.setBusinessId(businessId);

        model.addAttribute("review", review);
        model.addAttribute("business",
                reviewService.getBusinessInformation(businessId));
        model.addAttribute("photoUrlsText", "");

        return "ReviewFormPage";
    }

    @PostMapping("/create")
    public String createReview(@Valid @ModelAttribute("review") ReviewDTO review,
                               BindingResult bindingResult,
                               @RequestParam(required = false) String photoUrls,
                               Model model) {

        reviewService.applyPhotoUrls(review, photoUrls);

        if (bindingResult.hasErrors()) {
            model.addAttribute("business",
                    reviewService.getBusinessInformation(review.getBusinessId()));
            model.addAttribute("photoUrlsText", photoUrls);
            return "ReviewFormPage";
        }

        reviewService.createReview(review);

        return "redirect:/reviews/" + review.getBusinessId();
    }

    @GetMapping("/edit/{reviewId}")
    public String editReviewPage(@PathVariable String reviewId,
                                 Model model) {

        ReviewDTO review = reviewService.getReview(reviewId);

        if (review == null) {
            return "redirect:/reviews/BUS00";
        }

        model.addAttribute("review", review);
        model.addAttribute("business",
                reviewService.getBusinessInformation(review.getBusinessId()));
        model.addAttribute("photoUrlsText",
                reviewService.getPhotoUrlsText(review));

        return "ReviewEditPage";
    }

    @PostMapping("/update")
    public String updateReview(@Valid @ModelAttribute("review") ReviewDTO review,
                               BindingResult bindingResult,
                               @RequestParam(required = false) String photoUrls,
                               Model model) {

        ReviewDTO existingReview = reviewService.getReview(review.getReviewId());

        if (existingReview == null) {
            return "redirect:/reviews/BUS00";
        }

        review.setBusinessId(existingReview.getBusinessId());
        reviewService.applyPhotoUrls(review, photoUrls);

        if (bindingResult.hasErrors()) {
            model.addAttribute("business",
                    reviewService.getBusinessInformation(existingReview.getBusinessId()));
            model.addAttribute("photoUrlsText", photoUrls);
            return "ReviewEditPage";
        }

        reviewService.updateReview(review);

        return "redirect:/reviews/" + review.getBusinessId();
    }

    @GetMapping("/detail/{reviewId}")
        public String reviewDetailPage(@PathVariable String reviewId,
                                Model model) {

        ReviewDTO review = reviewService.getReview(reviewId);

        if (review == null) {
                return "redirect:/reviews/BUS00";
        }

        model.addAttribute("review", review);

        model.addAttribute(
                "business",
                reviewService.getBusinessInformation(review.getBusinessId())
        );

        // Temporary empty list until Comment module is connected.
        model.addAttribute("comments", java.util.Collections.emptyList());

        // Temporary until current-user authentication is connected.
        model.addAttribute("currentUserId", null);

        return "ReviewDetailPage";
        }

    @PostMapping("/delete/{reviewId}")
    public String deleteReview(@PathVariable String reviewId,
                               @RequestParam String businessId) {

        reviewService.deleteReview(reviewId);

        return "redirect:/reviews/" + businessId;
    }

}
