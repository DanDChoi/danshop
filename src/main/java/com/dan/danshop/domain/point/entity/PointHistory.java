package com.dan.danshop.domain.point.entity;

import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "point_history", indexes = {
        @Index(name = "idx_point_user_id", columnList = "user_id")
})
public class PointHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Long amount; // always positive; direction determined by type

    @Enumerated(EnumType.STRING)
    private PointType type;

    private String description;

    private Long orderId;
}
