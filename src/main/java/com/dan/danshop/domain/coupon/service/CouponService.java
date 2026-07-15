package com.dan.danshop.domain.coupon.service;

import com.dan.danshop.domain.coupon.dto.CouponResponse;
import com.dan.danshop.domain.coupon.dto.MyCouponResponse;
import com.dan.danshop.domain.coupon.entity.Coupon;
import com.dan.danshop.domain.coupon.entity.UserCoupon;
import com.dan.danshop.domain.coupon.repository.CouponRepository;
import com.dan.danshop.domain.coupon.repository.UserCouponRepository;
import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.domain.user.repository.UserRepository;
import com.dan.danshop.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.dan.danshop.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String COUPON_KEY = "coupon:";
    private static final String COUPON_ISSUED_KEY = "coupon:issued:";

    // SADD + DECR 원자적 처리 Lua Script
    // 반환값: 0 = 중복 발급, -1 = 수량 소진, 1 = 발급 성공
    private static final DefaultRedisScript<Long> ISSUE_COUPON_SCRIPT = new DefaultRedisScript<>("""
            local added = redis.call('SADD', KEYS[1], ARGV[1])
            if added == 0 then return 0 end
            local remain = redis.call('DECR', KEYS[2])
            if remain < 0 then
                redis.call('SREM', KEYS[1], ARGV[1])
                return -1
            end
            return 1
            """, Long.class);

    public void createCoupon(Coupon coupon) {
        couponRepository.save(coupon);
        redisTemplate.opsForValue().set(COUPON_KEY + coupon.getId(), coupon.getTotalQuantity());
    }

    @Transactional
    public void issueCoupon(Long couponId, String userId) {
        // 1. 유저 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        // 2. Lua Script로 중복 체크 + 수량 차감 원자적 처리
        Long result = redisTemplate.execute(
                ISSUE_COUPON_SCRIPT,
                List.of(COUPON_ISSUED_KEY + couponId, COUPON_KEY + couponId),
                userId
        );
        if (result == null || result == 0L) throw new BusinessException(COUPON_ALREADY_ISSUED);
        if (result == -1L) throw new BusinessException(COUPON_SOLD_OUT);

        // 3. 쿠폰 조회 + DB remainQuantity 차감
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(COUPON_NOT_FOUND));
        coupon.decrementRemain();

        // 4. UserCoupon 저장
        UserCoupon userCoupon = UserCoupon.builder()
                .user(user)
                .coupon(coupon)
                .isUsed(false)
                .build();
        userCouponRepository.save(userCoupon);
    }

    public List<CouponResponse> getAvailableCoupons() {
        return couponRepository
                .findByRemainQuantityGreaterThanAndExpiresAtAfter(0, LocalDateTime.now())
                .stream()
                .map(CouponResponse::from)
                .toList();
    }

    public List<MyCouponResponse> getMyCoupons() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
        return userCouponRepository.findByUserWithCoupon(user)
                .stream()
                .map(MyCouponResponse::from)
                .toList();
    }
}
