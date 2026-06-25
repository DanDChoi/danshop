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
    COUPON_ALREADY_ISSUED("이미 발급받은 쿠폰입니다."),
    COUPON_ALREADY_USED("이미 사용된 쿠폰입니다."),
    COUPON_EXPIRED("만료된 쿠폰입니다."),
    COUPON_NOT_OWNED("보유하지 않은 쿠폰입니다."),
    MIN_ORDER_AMOUNT_NOT_MET("최소 주문금액을 충족하지 않습니다."),
    INVALID_REFRESH_TOKEN("유효하지 않은 Refresh Token입니다."),
    REFRESH_TOKEN_NOT_FOUND("Refresh Token이 존재하지 않습니다."),
    CART_ITEM_NOT_FOUND("장바구니에 존재하지 않는 상품입니다."),
    INSUFFICIENT_POINTS("포인트가 부족합니다."),
    REVIEW_NOT_FOUND("리뷰를 찾을 수 없습니다."),
    DUPLICATE_REVIEW("이미 작성한 리뷰입니다."),
    NOT_PURCHASED_PRODUCT("구매한 상품만 리뷰를 작성할 수 있습니다."),
    NOT_REVIEW_OWNER("본인의 리뷰만 수정/삭제할 수 있습니다."),
    INVALID_RATING("평점은 1~5 사이여야 합니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
