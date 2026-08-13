package com.dan.danshop.domain.order.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GuestOrderRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private GuestOrderRequest valid() {
        return new GuestOrderRequest(
                "홍길동",
                "guest@test.com",
                "010-1234-5678",
                BigDecimal.valueOf(10000),
                "12345",
                "서울시 강남구",
                "101호",
                List.of(new OrderItemRequest(1L, 1))
        );
    }

    @Test
    void 모든_필드가_유효하면_위반이_없다() {
        Set<ConstraintViolation<GuestOrderRequest>> violations = validator.validate(valid());
        assertThat(violations).isEmpty();
    }

    @Test
    void 주문자_이름이_비어있으면_위반된다() {
        GuestOrderRequest request = new GuestOrderRequest(
                "", "guest@test.com", "010-1234-5678",
                BigDecimal.valueOf(10000), "12345", "서울시 강남구", "101호",
                List.of(new OrderItemRequest(1L, 1))
        );
        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void 이메일_형식이_아니면_위반된다() {
        GuestOrderRequest request = new GuestOrderRequest(
                "홍길동", "not-an-email", "010-1234-5678",
                BigDecimal.valueOf(10000), "12345", "서울시 강남구", "101호",
                List.of(new OrderItemRequest(1L, 1))
        );
        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void 연락처가_비어있으면_위반된다() {
        GuestOrderRequest request = new GuestOrderRequest(
                "홍길동", "guest@test.com", "",
                BigDecimal.valueOf(10000), "12345", "서울시 강남구", "101호",
                List.of(new OrderItemRequest(1L, 1))
        );
        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void 배송지가_비어있으면_위반된다() {
        GuestOrderRequest request = new GuestOrderRequest(
                "홍길동", "guest@test.com", "010-1234-5678",
                BigDecimal.valueOf(10000), "", "", "",
                List.of(new OrderItemRequest(1L, 1))
        );
        assertThat(validator.validate(request)).hasSize(3);
    }

    @Test
    void 결제금액이_0이하면_위반된다() {
        GuestOrderRequest request = new GuestOrderRequest(
                "홍길동", "guest@test.com", "010-1234-5678",
                BigDecimal.ZERO, "12345", "서울시 강남구", "101호",
                List.of(new OrderItemRequest(1L, 1))
        );
        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void 주문_상품이_비어있으면_위반된다() {
        GuestOrderRequest request = new GuestOrderRequest(
                "홍길동", "guest@test.com", "010-1234-5678",
                BigDecimal.valueOf(10000), "12345", "서울시 강남구", "101호",
                List.of()
        );
        assertThat(validator.validate(request)).isNotEmpty();
    }
}
