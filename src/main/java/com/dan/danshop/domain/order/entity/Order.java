package com.dan.danshop.domain.order.entity;

import com.dan.danshop.domain.order.dto.CreateRequest;
import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.global.common.BaseEntity;
import com.dan.danshop.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

import static com.dan.danshop.global.exception.ErrorCode.IMPOSSIBLE_CANCEL_ORDER;

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
    private String postNo;
    private String baseAddr;
    private String detailAddr;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public static Order from(CreateRequest createRequest, User user, BigDecimal payAmount) {
        return Order.builder()
                .status(OrderStatus.PENDING)
                .payAmount(payAmount)
                .postNo(createRequest.getPostNo())
                .baseAddr(createRequest.getBaseAddr())
                .detailAddr(createRequest.getDetailAddr())
                .user(user)
                .build();
    }

    public void cancel() {
        if (this.status != OrderStatus.PENDING) {
            throw new BusinessException(IMPOSSIBLE_CANCEL_ORDER);
        }
        this.status = OrderStatus.CANCELLED;
    }
}
