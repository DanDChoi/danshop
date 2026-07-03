package com.dan.danshop.domain.review.controller;

import com.dan.danshop.domain.review.dto.CreateReviewRequest;
import com.dan.danshop.domain.review.dto.ProductReviewResponse;
import com.dan.danshop.domain.review.dto.UpdateReviewRequest;
import com.dan.danshop.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<Map<String, Long>> createReview(
            @PathVariable Long productId,
            @Valid @RequestBody CreateReviewRequest request) {
        Long reviewId = reviewService.createReview(productId, request);
        return ResponseEntity.ok(Map.of("reviewId", reviewId));
    }

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<ProductReviewResponse> getReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviews(productId));
    }

    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        reviewService.updateReview(reviewId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
