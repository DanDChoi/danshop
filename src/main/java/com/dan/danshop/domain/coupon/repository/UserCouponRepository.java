package com.dan.danshop.domain.coupon.repository;

import com.dan.danshop.domain.coupon.entity.UserCoupon;
import com.dan.danshop.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);
    Optional<UserCoupon> findByUserIdAndCouponId(Long userId, Long couponId);

    @Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.coupon WHERE uc.user = :user")
    List<UserCoupon> findByUserWithCoupon(@Param("user") User user);
}
