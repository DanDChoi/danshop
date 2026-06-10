package com.dan.danshop.domain.coupon.service;

import com.dan.danshop.domain.coupon.entity.Coupon;
import com.dan.danshop.domain.coupon.entity.DiscountType;
import com.dan.danshop.domain.coupon.entity.UserCoupon;
import com.dan.danshop.domain.coupon.repository.CouponRepository;
import com.dan.danshop.domain.coupon.repository.UserCouponRepository;
import com.dan.danshop.domain.user.entity.Role;
import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.domain.user.repository.UserRepository;
import com.dan.danshop.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class CouponServiceTest {

    @Autowired private CouponService couponService;
    @Autowired private CouponRepository couponRepository;
    @Autowired private UserCouponRepository userCouponRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RedisTemplate<String, Object> redisTemplate;

    private static final String COUPON_KEY = "coupon:";

    @BeforeEach
    void setUp() {
        userCouponRepository.deleteAll();
        couponRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ───────────────────────────────────────────
    // 1단계: 쿠폰 생성 + Redis 수량 초기화 확인
    // ───────────────────────────────────────────

    @Test
    void 쿠폰_생성시_DB에_저장되고_Redis에_수량이_초기화된다() {
        // given
        Coupon coupon = Coupon.builder()
                .name("테스트 쿠폰")
                .discountType(DiscountType.AMOUNT)
                .discountValue(BigDecimal.valueOf(5000))
                .minOrderAmount(BigDecimal.valueOf(30000))
                .totalQuantity(100)
                .remainQuantity(100)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        // when
        couponService.createCoupon(coupon);

        // then - DB 저장 확인
        Coupon saved = couponRepository.findById(coupon.getId()).orElseThrow();
        assertThat(saved.getName()).isEqualTo("테스트 쿠폰");
        assertThat(saved.getTotalQuantity()).isEqualTo(100);

        // then - Redis 수량 초기화 확인
        Object redisValue = redisTemplate.opsForValue().get(COUPON_KEY + coupon.getId());
        assertThat(redisValue).isNotNull();
        assertThat(Integer.parseInt(redisValue.toString())).isEqualTo(100);

        System.out.println("DB 저장된 쿠폰명: " + saved.getName());
        System.out.println("Redis 초기 수량: " + redisValue);
    }
}
