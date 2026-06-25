package com.dan.danshop.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProductReviewResponse {
    private Double avgRating;
    private int reviewCount;
    private List<ReviewResponse> reviews;
}
