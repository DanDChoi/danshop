package com.dan.danshop.domain.coupon.controller;

import com.dan.danshop.domain.coupon.dto.CouponCreateRequest;
import com.dan.danshop.domain.coupon.entity.Coupon;
import com.dan.danshop.domain.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/coupons")
    public ResponseEntity<?> createCoupon(@RequestBody CouponCreateRequest createRequest) {

        Coupon coupon = Coupon.builder()
                .discountType(createRequest.getDiscountType())
                .discountValue(createRequest.getDiscountValue())
                .minOrderAmount(createRequest.getMinOrderAmount())
                .totalQuantity(createRequest.getTotalQuantity())
                .remainQuantity(createRequest.getRemainQuantity())
                .expiresAt(createRequest.getExpiresAt())
                .build();
        couponService.createCoupon(coupon);
    }
}
