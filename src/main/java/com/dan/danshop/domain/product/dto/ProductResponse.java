package com.dan.danshop.domain.product.dto;

import com.dan.danshop.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private String productName;
    private BigDecimal price;
    private String category;
    private int stock;
    private String description;


    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .productName(product.getProductName())
                .price(product.getPrice())
                .category(product.getCategory())
                .stock(product.getStock())
                .description(product.getDescription())
                .build();
    }
}
