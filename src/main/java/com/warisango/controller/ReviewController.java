package com.warisango.controller;

import com.warisango.dto.ReviewDTO;
import com.warisango.model.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

        model.addAttribute("business",
                reviewService.getBusinessInformation(businessId));

        model.addAttribute("reviews",
                reviewService.getReviewsByBusiness(businessId));

        model.addAttribute("averageRating",
                reviewService.getAverageRating(businessId));

        model.addAttribute("totalReviews",
                reviewService.getTotalReviews(businessId));

        return "ReviewAndRatingPage";
    }

    @GetMapping("/create/{businessId}")
    public String createReviewPage(@PathVariable String businessId,
                                   Model model) {

        ReviewDTO review = new ReviewDTO();

        review.setBusinessId(businessId);

        model.addAttribute("review", review);

        return "ReviewFormPage";
    }

    @PostMapping("/create")
    public String createReview(@ModelAttribute ReviewDTO review) {

        reviewService.createReview(review);

        return "redirect:/reviews/" + review.getBusinessId();
    }

    @GetMapping("/edit/{reviewId}")
    public String editReviewPage(@PathVariable String reviewId,
                                 Model model) {

        model.addAttribute("review",
                reviewService.getReview(reviewId));

        return "ReviewEditPage";
    }

    @PostMapping("/update")
    public String updateReview(@ModelAttribute ReviewDTO review) {

        reviewService.updateReview(review);

        return "redirect:/reviews/" + review.getBusinessId();
    }

    @PostMapping("/delete/{reviewId}")
    public String deleteReview(@PathVariable String reviewId,
                               @RequestParam String businessId) {

        reviewService.deleteReview(reviewId);

        return "redirect:/reviews/" + businessId;
    }

}