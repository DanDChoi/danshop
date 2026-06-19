package com.dan.danshop.domain.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class CartResponse {
    private List<CartItem> items;
    private BigDecimal totalAmount;
}
