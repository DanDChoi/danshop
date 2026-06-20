package com.dan.danshop.domain.point.entity;

public enum PointType {
    EARN,        // 적립
    USE,         // 사용
    EARN_CANCEL, // 적립 취소 (주문 취소 시)
    REFUND       // 사용 환불 (주문 취소 시)
}
