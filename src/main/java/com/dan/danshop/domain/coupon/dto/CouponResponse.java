package com.dan.danshop.domain.coupon.dto;

import com.dan.danshop.domain.coupon.entity.Coupon;
import com.dan.danshop.domain.coupon.entity.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CouponResponse {
    private Long id;
    private String name;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private int remainQuantity;
    private LocalDateTime expiresAt;

    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getName(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getMinOrderAmount(),
                coupon.getRemainQuantity(),
                coupon.getExpiresAt()
        );
    }
}
