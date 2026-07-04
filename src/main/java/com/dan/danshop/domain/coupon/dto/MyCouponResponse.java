package com.dan.danshop.domain.coupon.dto;

import com.dan.danshop.domain.coupon.entity.Coupon;
import com.dan.danshop.domain.coupon.entity.DiscountType;
import com.dan.danshop.domain.coupon.entity.UserCoupon;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MyCouponResponse {
    private Long couponId;
    private String name;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private LocalDateTime expiresAt;
    private boolean used;

    public static MyCouponResponse from(UserCoupon userCoupon) {
        Coupon coupon = userCoupon.getCoupon();
        return new MyCouponResponse(
                coupon.getId(),
                coupon.getName(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getMinOrderAmount(),
                coupon.getExpiresAt(),
                userCoupon.isUsed()
        );
    }
}
