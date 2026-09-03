package com.warisango.controller;

import com.warisango.dto.LikeStatusDTO;
import com.warisango.service.CommentLikeService;
import com.warisango.service.CommentService;
import com.warisango.service.ReviewLikeService;
import com.warisango.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
public class ReviewLikeController {

    private final ReviewService reviewService;
    private final CommentService commentService;
    private final ReviewLikeService reviewLikeService;
    private final CommentLikeService commentLikeService;

    public ReviewLikeController(
            ReviewService reviewService,
            CommentService commentService,
            ReviewLikeService reviewLikeService,
            CommentLikeService commentLikeService) {
        this.reviewService = reviewService;
        this.commentService = commentService;
        this.reviewLikeService = reviewLikeService;
        this.commentLikeService = commentLikeService;
    }

    @PostMapping("/like/{reviewId}")
    public ResponseEntity<LikeStatusDTO> toggleReviewLike(
            @PathVariable String reviewId,
            Authentication authentication) {
        if (reviewService.getReview(reviewId) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reviewLikeService.toggleLike(reviewId, authentication.getName()));
    }

    @PostMapping("/comments/like/{commentId}")
    public ResponseEntity<LikeStatusDTO> toggleCommentLike(
            @PathVariable String commentId,
            Authentication authentication) {
        if (commentService.getComment(commentId) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(commentLikeService.toggleLike(commentId, authentication.getName()));
    }
}
