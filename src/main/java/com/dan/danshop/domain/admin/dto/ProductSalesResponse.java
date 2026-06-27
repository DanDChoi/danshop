package com.dan.danshop.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ProductSalesResponse {
    private Long productId;
    private String productName;
    private Integer totalQuantity;
    private BigDecimal totalRevenue;
}
