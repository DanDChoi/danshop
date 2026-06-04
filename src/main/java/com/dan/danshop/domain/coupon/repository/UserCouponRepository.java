package com.dan.danshop.domain.coupon.repository;

import com.dan.danshop.domain.coupon.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);
    Optional<UserCoupon> findByUserIdAndCouponId(Long userId, Long couponId);
}
