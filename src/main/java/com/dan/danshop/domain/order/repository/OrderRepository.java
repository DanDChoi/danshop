package com.dan.danshop.domain.order.repository;

import com.dan.danshop.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {

//    Page<Order> findByOrderNameContaining(String keyword, Pageable pageable);

    //N+1 해결
    @Query("SELECT o FROM Order o JOIN FETCH o.user")
    Page<Order> findAllWithUser(Pageable pageable);
}
