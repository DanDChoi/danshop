package com.dan.danshop.domain.order.entity;

import com.dan.danshop.domain.order.dto.CreateRequest;
import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private BigDecimal payAmount;
    private String postNo;
    private String baseAddr;
    private String detailAddr;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public static Order from(CreateRequest createRequest, User user) {
        return Order.builder()
                .status(OrderStatus.PENDING)
                .payAmount(createRequest.getPayAmount())
                .postNo(createRequest.getPostNo())
                .baseAddr(createRequest.getBaseAddr())
                .detailAddr(createRequest.getDetailAddr())
                .user(user)
                .build();
    }

    public void cancel () {
        if (this.status != OrderStatus.PENDING) {
            throw new RuntimeException("취소 가능한 주문이 아닙니다");
        }
        this.status = OrderStatus.CANCELLED;
    }
}
