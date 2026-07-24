package com.dan.danshop.domain.review.controller;

import com.dan.danshop.domain.review.dto.CreateReviewRequest;
import com.dan.danshop.domain.review.dto.ProductReviewResponse;
import com.dan.danshop.domain.review.dto.UpdateReviewRequest;
import com.dan.danshop.domain.review.service.ReviewService;
import com.dan.danshop.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Review", description = "리뷰 관련 API")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/products/{productId}/reviews")
    @Operation(summary = "리뷰 작성")
    @ApiResponse(responseCode = "200", description = "작성 성공")
    @ApiResponse(responseCode = "403", description = "구매하지 않은 상품",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "이미 작성한 리뷰",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Map<String, Long>> createReview(
            @PathVariable Long productId,
            @Valid @RequestBody CreateReviewRequest request) {
        Long reviewId = reviewService.createReview(productId, request);
        return ResponseEntity.ok(Map.of("reviewId", reviewId));
    }

    @GetMapping("/products/{productId}/reviews")
    @Operation(summary = "상품 리뷰 목록 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<ProductReviewResponse> getReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviews(productId));
    }

    @PatchMapping("/reviews/{reviewId}")
    @Operation(summary = "리뷰 수정")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "403", description = "본인 리뷰 아님",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        reviewService.updateReview(reviewId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reviews/{reviewId}")
    @Operation(summary = "리뷰 삭제")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ApiResponse(responseCode = "403", description = "본인 리뷰 아님",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
