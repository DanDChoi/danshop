package com.dan.danshop.domain.order.dto;

import com.dan.danshop.domain.order.entity.Order;
import com.dan.danshop.domain.order.entity.OrderItem;
import com.dan.danshop.domain.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class OrderDetailResponse {
    private Long orderId;
    private OrderStatus status;
    private BigDecimal payAmount;
    private String postNo;
    private String baseAddr;
    private String detailAddr;
    private List<OrderItemResponse> items;

    public static OrderDetailResponse from(Order order, List<OrderItem> orderItems) {
        return OrderDetailResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .payAmount(order.getPayAmount())
                .postNo(order.getPostNo())
                .baseAddr(order.getBaseAddr())
                .detailAddr(order.getDetailAddr())
                .items(orderItems.stream().map(OrderItemResponse::from).toList())
                .build();
    }
}
