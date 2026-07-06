package com.dan.danshop.domain.cart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CartCheckoutRequest {
    private Long couponId;
    private Long usePoints;

    @NotBlank
    private String postNo;

    @NotBlank
    private String baseAddr;

    @NotBlank
    private String detailAddr;
}
