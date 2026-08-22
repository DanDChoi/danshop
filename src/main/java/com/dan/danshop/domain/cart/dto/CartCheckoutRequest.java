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

    // 비회원 checkout에서만 사용. 회원은 무시됨.
    private String ordererName;
    private String ordererEmail;
    private String ordererPhone;
}
