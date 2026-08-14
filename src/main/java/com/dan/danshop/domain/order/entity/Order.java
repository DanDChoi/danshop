package com.dan.danshop.domain.order.entity;

import com.dan.danshop.domain.order.dto.CreateRequest;
import com.dan.danshop.domain.order.dto.GuestOrderRequest;
import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.global.common.BaseEntity;
import com.dan.danshop.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

import java.util.Set;
import java.util.Map;

import static com.dan.danshop.global.exception.ErrorCode.ADDRESS_CHANGE_NOT_ALLOWED;
import static com.dan.danshop.global.exception.ErrorCode.IMPOSSIBLE_CANCEL_ORDER;
import static com.dan.danshop.global.exception.ErrorCode.INVALID_ORDER_STATUS_TRANSITION;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id")
})
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private BigDecimal payAmount;
    private Long couponId;
    private String postNo;
    private String baseAddr;
    private String detailAddr;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
    @Embedded
    private Orderer orderer;

    public static Order from(CreateRequest createRequest, User user, BigDecimal payAmount) {
        return Order.builder()
                .status(OrderStatus.PENDING)
                .payAmount(payAmount)
                .couponId(createRequest.getCouponId())
                .postNo(createRequest.getPostNo())
                .baseAddr(createRequest.getBaseAddr())
                .detailAddr(createRequest.getDetailAddr())
                .user(user)
                .build();
    }

    public static Order fromGuest(GuestOrderRequest guestOrderRequest, BigDecimal payAmount) {
        return Order.builder()
                .status(OrderStatus.PENDING)
                .payAmount(payAmount)
                .postNo(guestOrderRequest.getPostNo())
                .baseAddr(guestOrderRequest.getBaseAddr())
                .detailAddr(guestOrderRequest.getDetailAddr())
                .orderer(new Orderer(
                        guestOrderRequest.getOrdererName(),
                        guestOrderRequest.getOrdererEmail(),
                        guestOrderRequest.getOrdererPhone()
                ))
                .build();
    }

    public void cancel() {
        if (this.status != OrderStatus.PENDING) {
            throw new BusinessException(IMPOSSIBLE_CANCEL_ORDER);
        }
        this.status = OrderStatus.CANCELLED;
    }

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.PAID, OrderStatus.CANCELLED),
            OrderStatus.PAID, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
            OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED)
    );

    public void updateAddress(String postNo, String baseAddr, String detailAddr) {
        if (this.status != OrderStatus.PENDING) {
            throw new BusinessException(ADDRESS_CHANGE_NOT_ALLOWED);
        }
        this.postNo = postNo;
        this.baseAddr = baseAddr;
        this.detailAddr = detailAddr;
    }

    public void updateStatus(OrderStatus newStatus) {
        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(this.status, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new BusinessException(INVALID_ORDER_STATUS_TRANSITION);
        }
        this.status = newStatus;
    }
}
