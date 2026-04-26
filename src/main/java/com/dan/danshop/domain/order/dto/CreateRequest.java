package com.dan.danshop.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRequest {
    private BigDecimal payAmount;
    private String postNo;
    private String baseAddr;
    private String detailAddr;
    private List<OrderItemRequest> items;
}
