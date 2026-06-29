package com.dan.danshop.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationEvent {
    private String type;    // ORDER_CREATED, ORDER_CANCELLED, POINT_EARNED, POINT_USED
    private String message;
    private Object data;    // optional payload
}
