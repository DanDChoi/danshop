package com.dan.danshop.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddRequest {
    private String productName;
    private BigDecimal price;
    private String category;
    private int stock;
    private String description;
}
