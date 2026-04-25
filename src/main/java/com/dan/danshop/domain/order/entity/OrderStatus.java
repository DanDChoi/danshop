package com.dan.danshop.domain.order.entity;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("주문 대기"),
    PAID("결제 완료"),
    CANCELLED("주문 취소");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}
