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

    public void createCoupon(Coupon coupon) {
        couponRepository.save(coupon);
        redisTemplate.opsForValue().set(COUPON_KEY + coupon.getId(), coupon.getTotalQuantity());
    }

    @Transactional
    public void issueCoupon(Long couponId, String userId) {
        // 1. 유저 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        // 2. 중복 발급 체크
        if (userCouponRepository.existsByUserIdAndCouponId(user.getId(), couponId)) {
            throw new BusinessException(COUPON_ALREADY_ISSUED);
        }

        // 3. Redis에서 수량 차감 (원자적 연산)
        Long remain = redisTemplate.opsForValue().decrement(COUPON_KEY + couponId);
        if (remain == null || remain < 0) {
            throw new BusinessException(COUPON_SOLD_OUT);
        }

        // 4. 쿠폰 조회
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(COUPON_NOT_FOUND));

        // 5. UserCoupon 저장
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
