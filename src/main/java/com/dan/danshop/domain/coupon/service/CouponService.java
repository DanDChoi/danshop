package com.dan.danshop.domain.coupon.service;

import com.dan.danshop.domain.coupon.entity.Coupon;
import com.dan.danshop.domain.coupon.entity.UserCoupon;
import com.dan.danshop.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final RedisTemplate<Object, Object> redisTemplate;

    public void createCoupon(Coupon coupon) {
        redisTemplate.opsForValue().set("coupon: " + coupon.getId(), coupon.getTotalQuantity());
        couponRepository.save(coupon);
    }

    @Transactional
    public void issueCoupon(Long couponId) {
        Long remain = redisTemplate.opsForValue().decrement("coupon: " + couponId);
        if (remain < 0) {
            //마감
        }
        //성공 -> db에 userCoupon 저장
    }
}
