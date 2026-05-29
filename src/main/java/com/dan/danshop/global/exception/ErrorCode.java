package com.dan.danshop.global.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    DUPLICATED_USER_ID("이미 존재하는 아이디입니다."),
    PASSWORD_NOT_MATCH("비밀번호가 일치하지 않습니다."),
    PRODUCT_NOT_FOUND("상품을 찾을 수 없습니다."),
    ORDER_NOT_FOUND("주문을 찾을 수 없습니다."),
    IMPOSSIBLE_CANCEL_ORDER("취소 가능한 주문이 아닙니다."),
    NOT_ORDERED_USER("본인의 주문만 취소 가능합니다."),
    COUPON_NOT_FOUND("쿠폰을 찾을 수 없습니다."),
    COUPON_SOLD_OUT("선착순 마감된 쿠폰입니다."),
    COUPON_ALREADY_ISSUED("이미 발급받은 쿠폰입니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
