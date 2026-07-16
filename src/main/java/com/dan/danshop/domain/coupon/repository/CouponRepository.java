package com.dan.danshop.domain.coupon.repository;

import com.dan.danshop.domain.coupon.entity.Coupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p From Coupon p WHERE p.id = :id")
    Optional<Coupon> findByIdWithLock(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Coupon c SET c.remainQuantity = c.remainQuantity - 1 WHERE c.id = :id")
    void decrementRemainById(@Param("id") Long id);

    List<Coupon> findByRemainQuantityGreaterThanAndExpiresAtAfter(int remainQuantity, LocalDateTime now);
}
