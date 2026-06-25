package com.dan.danshop.domain.review.entity;

import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reviews", indexes = {
        @Index(name = "idx_review_product_id", columnList = "product_id"),
        @Index(name = "idx_review_user_id", columnList = "user_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_product", columnNames = {"user_id", "product_id"})
})
public class Review extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int rating; // 1~5

    private String content;

    public void update(int rating, String content) {
        this.rating = rating;
        this.content = content;
    }
}
