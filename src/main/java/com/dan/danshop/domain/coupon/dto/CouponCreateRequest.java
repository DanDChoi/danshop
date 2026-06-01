package com.dan.danshop.domain.coupon.dto;

import com.dan.danshop.domain.coupon.entity.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponCreateRequest {
    private String name;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private int totalQuantity;
    private LocalDateTime expiresAt;
}
