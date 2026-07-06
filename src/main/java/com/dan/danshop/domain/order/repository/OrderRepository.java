package com.dan.danshop.domain.order.repository;

import com.dan.danshop.domain.order.entity.Order;
import com.dan.danshop.domain.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.user.userId = :userId")
    Page<Order> findByUserIdString(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT o FROM Order o JOIN FETCH o.user")
    Page<Order> findAllWithUser(Pageable pageable);

    @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE (:status IS NULL OR o.status = :status)")
    Page<Order> findAllWithUserAndStatus(@Param("status") OrderStatus status, Pageable pageable);
}
