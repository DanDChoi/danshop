package com.dan.danshop.domain.review.dto;

import com.dan.danshop.domain.review.entity.Review;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewResponse {
    private final Long id;
    private final String userId;
    private final String userName;
    private final int rating;
    private final String content;
    private final LocalDateTime createdAt;

    private ReviewResponse(Review review) {
        this.id = review.getId();
        this.userId = review.getUser().getUserId();
        this.userName = review.getUser().getName();
        this.rating = review.getRating();
        this.content = review.getContent();
        this.createdAt = review.getCreatedAt();
    }

    public static ReviewResponse from(Review review) {
        return new ReviewResponse(review);
    }
}
