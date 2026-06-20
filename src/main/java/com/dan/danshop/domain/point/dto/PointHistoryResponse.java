package com.dan.danshop.domain.point.dto;

import com.dan.danshop.domain.point.entity.PointHistory;
import com.dan.danshop.domain.point.entity.PointType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PointHistoryResponse {
    private final Long id;
    private final long signedAmount; // positive = credit, negative = debit
    private final PointType type;
    private final String description;
    private final Long orderId;
    private final LocalDateTime createdAt;

    private PointHistoryResponse(PointHistory h) {
        this.id = h.getId();
        this.signedAmount = switch (h.getType()) {
            case EARN, REFUND -> h.getAmount();
            case USE, EARN_CANCEL -> -h.getAmount();
        };
        this.type = h.getType();
        this.description = h.getDescription();
        this.orderId = h.getOrderId();
        this.createdAt = h.getCreatedAt();
    }

    public static PointHistoryResponse from(PointHistory h) {
        return new PointHistoryResponse(h);
    }
}
