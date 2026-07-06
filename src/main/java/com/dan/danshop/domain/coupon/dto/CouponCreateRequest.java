package com.dan.danshop.domain.coupon.dto;

import com.dan.danshop.domain.coupon.entity.DiscountType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponCreateRequest {
    @NotBlank
    private String name;

    @NotNull
    private DiscountType discountType;

    @NotNull
    @Positive
    private BigDecimal discountValue;

    @NotNull
    private BigDecimal minOrderAmount;

    @Min(1)
    private int totalQuantity;

    @NotNull
    @Future
    private LocalDateTime expiresAt;
}
