package com.dan.danshop.domain.order.dto;

import com.dan.danshop.domain.order.entity.Order;
import com.dan.danshop.domain.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private OrderStatus status;
    private BigDecimal payAmount;
    private String postNo;
    private String baseAddr;
    private String detailAddr;
    private String userId;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .status(order.getStatus())
                .payAmount(order.getPayAmount())
                .postNo(order.getPostNo())
                .baseAddr(order.getBaseAddr())
                .detailAddr(order.getDetailAddr())
                .userId(order.getUser().getUserId())
                .build();
    }
}
