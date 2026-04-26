package com.dan.danshop.domain.order.repository;

import com.dan.danshop.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
