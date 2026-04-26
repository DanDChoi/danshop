package com.dan.danshop.domain.order.entity;

import com.dan.danshop.domain.order.dto.OrderItemRequest;
import com.dan.danshop.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal price;
    private int quantity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;


    public static OrderItem from(Order order, Product product, int quantity) {
        return OrderItem.builder()
                .price(product.getPrice())
                .quantity(quantity)
                .product(product)
                .order(order)
                .build();
    }
}
