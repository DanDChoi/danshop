package com.dan.danshop.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    DUPLICATED_USER_ID(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다."),
    PASSWORD_NOT_MATCH(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),

    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    OUT_OF_STOCK(HttpStatus.CONFLICT, "재고가 부족합니다."),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    IMPOSSIBLE_CANCEL_ORDER(HttpStatus.BAD_REQUEST, "취소 가능한 주문이 아닙니다."),
    NOT_ORDERED_USER(HttpStatus.FORBIDDEN, "본인의 주문만 취소 가능합니다."),
    INVALID_ORDER_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "허용되지 않는 주문 상태 변경입니다."),
    ADDRESS_CHANGE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "배송 준비 전 주문만 배송지를 변경할 수 있습니다."),

    // Coupon
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "쿠폰을 찾을 수 없습니다."),
    COUPON_SOLD_OUT(HttpStatus.CONFLICT, "선착순 마감된 쿠폰입니다."),
    COUPON_ALREADY_ISSUED(HttpStatus.CONFLICT, "이미 발급받은 쿠폰입니다."),
    COUPON_ALREADY_USED(HttpStatus.CONFLICT, "이미 사용된 쿠폰입니다."),
    COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "만료된 쿠폰입니다."),
    COUPON_NOT_OWNED(HttpStatus.FORBIDDEN, "보유하지 않은 쿠폰입니다."),
    MIN_ORDER_AMOUNT_NOT_MET(HttpStatus.BAD_REQUEST, "최소 주문금액을 충족하지 않습니다."),

    // Auth
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Refresh Token이 존재하지 않습니다."),

    // Cart
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니에 존재하지 않는 상품입니다."),
    GUEST_TOKEN_REQUIRED(HttpStatus.BAD_REQUEST, "비회원은 X-Guest-Token 헤더가 필요합니다."),
    ORDERER_INFO_REQUIRED(HttpStatus.BAD_REQUEST, "비회원 주문은 주문자 이름/이메일/연락처가 필요합니다."),

    // Point
    INSUFFICIENT_POINTS(HttpStatus.BAD_REQUEST, "포인트가 부족합니다."),

    // Review
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),
    DUPLICATE_REVIEW(HttpStatus.CONFLICT, "이미 작성한 리뷰입니다."),
    NOT_PURCHASED_PRODUCT(HttpStatus.FORBIDDEN, "구매한 상품만 리뷰를 작성할 수 있습니다."),
    NOT_REVIEW_OWNER(HttpStatus.FORBIDDEN, "본인의 리뷰만 수정/삭제할 수 있습니다."),
    INVALID_RATING(HttpStatus.BAD_REQUEST, "평점은 1~5 사이여야 합니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
