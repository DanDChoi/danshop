package com.dan.danshop.domain.coupon.repository;

import com.dan.danshop.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Modifying
    @Query("UPDATE Coupon c SET c.remainQuantity = c.remainQuantity - 1 WHERE c.id = :id")
    void decrementRemainById(@Param("id") Long id);

    List<Coupon> findByRemainQuantityGreaterThanAndExpiresAtAfter(int remainQuantity, LocalDateTime now);
}
