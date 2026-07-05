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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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

    // ───────────────────────────────────────────
    // 2단계: 정상 발급 / 중복 발급 방지 / 수량 소진
    // ───────────────────────────────────────────

    @Test
    void 쿠폰_정상_발급시_UserCoupon이_저장되고_Redis_수량이_감소한다() {
        // given
        User user = userRepository.save(User.builder()
                .userId("testuser")
                .email("test@test.com")
                .password("password")
                .name("테스트유저")
                .role(Role.ROLE_USER)
                .build());

        Coupon coupon = Coupon.builder()
                .name("5000원 할인 쿠폰")
                .discountType(DiscountType.AMOUNT)
                .discountValue(BigDecimal.valueOf(5000))
                .minOrderAmount(BigDecimal.valueOf(30000))
                .totalQuantity(10)
                .remainQuantity(10)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        couponService.createCoupon(coupon);

        // when
        couponService.issueCoupon(coupon.getId(), user.getUserId());

        // then - UserCoupon DB 저장 확인
        boolean exists = userCouponRepository.existsByUserIdAndCouponId(user.getId(), coupon.getId());
        assertThat(exists).isTrue();

        // then - Redis 수량 감소 확인 (10 → 9)
        Object redisValue = redisTemplate.opsForValue().get(COUPON_KEY + coupon.getId());
        assertThat(Integer.parseInt(redisValue.toString())).isEqualTo(9);

        System.out.println("Redis 남은 수량: " + redisValue);
    }

    @Test
    void 동일_유저가_같은_쿠폰을_두번_발급받으면_예외가_발생한다() {
        // given
        User user = userRepository.save(User.builder()
                .userId("testuser")
                .email("test@test.com")
                .password("password")
                .name("테스트유저")
                .role(Role.ROLE_USER)
                .build());

        Coupon coupon = Coupon.builder()
                .name("중복방지 쿠폰")
                .discountType(DiscountType.AMOUNT)
                .discountValue(BigDecimal.valueOf(3000))
                .minOrderAmount(BigDecimal.valueOf(20000))
                .totalQuantity(10)
                .remainQuantity(10)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        couponService.createCoupon(coupon);

        // when - 첫 번째 발급
        couponService.issueCoupon(coupon.getId(), user.getUserId());

        // then - 두 번째 발급 시 예외 발생
        assertThatThrownBy(() -> couponService.issueCoupon(coupon.getId(), user.getUserId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 발급받은 쿠폰입니다.");

        System.out.println("중복 발급 방지 확인 완료");
    }

    @Test
    void 수량이_소진된_쿠폰은_발급되지_않는다() {
        // given - 수량 1개짜리 쿠폰
        User user1 = userRepository.save(User.builder()
                .userId("user1")
                .email("user1@test.com")
                .password("password")
                .name("유저1")
                .role(Role.ROLE_USER)
                .build());

        User user2 = userRepository.save(User.builder()
                .userId("user2")
                .email("user2@test.com")
                .password("password")
                .name("유저2")
                .role(Role.ROLE_USER)
                .build());

        Coupon coupon = Coupon.builder()
                .name("1개짜리 쿠폰")
                .discountType(DiscountType.RATE)
                .discountValue(BigDecimal.valueOf(10))
                .minOrderAmount(BigDecimal.ZERO)
                .totalQuantity(1)
                .remainQuantity(1)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        couponService.createCoupon(coupon);

        // when - user1 발급 성공
        couponService.issueCoupon(coupon.getId(), user1.getUserId());

        // then - user2 발급 시 수량 소진 예외
        assertThatThrownBy(() -> couponService.issueCoupon(coupon.getId(), user2.getUserId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("선착순 마감된 쿠폰입니다.");

        System.out.println("user1 발급 성공, user2 수량 소진으로 실패 확인 완료");
    }

    // ───────────────────────────────────────────
    // 3단계: 동시성 테스트 - 100명이 동시에 요청해도 50개만 발급
    // ───────────────────────────────────────────

    @Test
    void 동시에_100명이_요청해도_쿠폰은_50개만_발급된다() throws InterruptedException {
        // given - 수량 50개짜리 쿠폰
        int totalQuantity = 50;
        int threadCount = 100;

        Coupon coupon = Coupon.builder()
                .name("선착순 50개 쿠폰")
                .discountType(DiscountType.AMOUNT)
                .discountValue(BigDecimal.valueOf(1000))
                .minOrderAmount(BigDecimal.ZERO)
                .totalQuantity(totalQuantity)
                .remainQuantity(totalQuantity)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        couponService.createCoupon(coupon);

        // 유저 100명 사전 생성
        List<User> users = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            users.add(userRepository.save(User.builder()
                    .userId("user" + i)
                    .email("user" + i + "@test.com")
                    .password("password")
                    .name("유저" + i)
                    .role(Role.ROLE_USER)
                    .build()));
        }

        // when - 100명 동시 요청
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        Long couponId = coupon.getId();

        for (int i = 0; i < threadCount; i++) {
            String userId = users.get(i).getUserId();
            executor.submit(() -> {
                try {
                    couponService.issueCoupon(couponId, userId);
                    successCount.incrementAndGet();
                } catch (BusinessException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // then - 정확히 50건만 성공, 50건은 실패
        assertThat(successCount.get()).isEqualTo(totalQuantity);
        assertThat(failCount.get()).isEqualTo(threadCount - totalQuantity);

        // then - DB에도 정확히 50건만 저장
        long issuedCount = userCouponRepository.count();
        assertThat(issuedCount).isEqualTo(totalQuantity);

        // then - Redis 수량: 50개 차감(성공) + 50개 차감(실패) = -50
        // decrement()는 원자적으로 감소만 할 뿐 음수를 막지 않으므로 최종값은 -(초과요청수)
        Object redisValue = redisTemplate.opsForValue().get(COUPON_KEY + couponId);
        assertThat(Integer.parseInt(redisValue.toString())).isEqualTo(-(threadCount - totalQuantity));

        System.out.println("성공: " + successCount.get() + "건, 실패: " + failCount.get() + "건");
        System.out.println("DB 발급 수량: " + issuedCount);
        System.out.println("Redis 최종 수량: " + redisValue);
    }
}
