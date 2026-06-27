package com.dan.danshop.domain.admin.dto;

import com.dan.danshop.domain.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderStatusStatResponse {
    private OrderStatus status;
    private Long count;
}
