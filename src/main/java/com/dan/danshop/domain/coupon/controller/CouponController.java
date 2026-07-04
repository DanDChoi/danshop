package com.dan.danshop.domain.coupon.controller;

import com.dan.danshop.domain.coupon.dto.CouponCreateRequest;
import com.dan.danshop.domain.coupon.dto.CouponResponse;
import com.dan.danshop.domain.coupon.dto.MyCouponResponse;
import com.dan.danshop.domain.coupon.entity.Coupon;
import com.dan.danshop.domain.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Coupon", description = "쿠폰 관련 API")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/coupons")
    @Operation(summary = "쿠폰 생성 (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCoupon(@Valid @RequestBody CouponCreateRequest createRequest) {
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

    @GetMapping("/coupons")
    @Operation(summary = "발급 가능한 쿠폰 목록 조회")
    public ResponseEntity<List<CouponResponse>> getAvailableCoupons() {
        return ResponseEntity.ok(couponService.getAvailableCoupons());
    }

    @GetMapping("/coupons/my")
    @Operation(summary = "내 쿠폰 목록 조회")
    public ResponseEntity<List<MyCouponResponse>> getMyCoupons() {
        return ResponseEntity.ok(couponService.getMyCoupons());
    }
}
