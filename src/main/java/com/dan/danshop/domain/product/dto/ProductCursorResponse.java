package com.dan.danshop.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProductCursorResponse {
    private List<ProductResponse> products;
    private boolean hasNext;
    private Long lastId;
}
