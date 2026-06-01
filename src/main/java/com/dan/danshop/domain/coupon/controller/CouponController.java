package com.dan.danshop.domain.coupon.controller;

import com.dan.danshop.domain.coupon.dto.CouponCreateRequest;
import com.dan.danshop.domain.coupon.entity.Coupon;
import com.dan.danshop.domain.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Coupon", description = "쿠폰 관련 API")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/coupons")
    @Operation(summary = "쿠폰 생성 (ADMIN)")
    public ResponseEntity<?> createCoupon(@RequestBody CouponCreateRequest createRequest) {
        Coupon coupon = Coupon.builder()
                .name(createRequest.getName())
                .discountType(createRequest.getDiscountType())
                .discountValue(createRequest.getDiscountValue())
                .minOrderAmount(createRequest.getMinOrderAmount())
                .totalQuantity(createRequest.getTotalQuantity())
                .remainQuantity(createRequest.getTotalQuantity())
                .expiresAt(createRequest.getExpiresAt())
                .build();
        couponService.createCoupon(coupon);
        return ResponseEntity.status(HttpStatus.CREATED).body("쿠폰 생성 완료");
    }

    @PostMapping("/coupons/{couponId}/issue")
    @Operation(summary = "쿠폰 선착순 발급")
    public ResponseEntity<?> issueCoupon(@PathVariable Long couponId, Authentication authentication) {
        couponService.issueCoupon(couponId, authentication.getName());
        return ResponseEntity.ok("쿠폰 발급 완료");
    }
}
