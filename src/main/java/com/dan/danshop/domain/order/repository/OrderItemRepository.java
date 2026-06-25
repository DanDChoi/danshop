package com.dan.danshop.domain.order.repository;

import com.dan.danshop.domain.order.entity.Order;
import com.dan.danshop.domain.order.entity.OrderItem;
import com.dan.danshop.domain.order.entity.OrderStatus;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder(Order order);

    boolean existsByOrder_UserAndProductAndOrder_StatusNot(User user, Product product, OrderStatus status);
}
