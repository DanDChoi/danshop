package com.dan.danshop.domain.wishlist.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class WishlistResponse {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private String category;
    private Double avgRating;
}
