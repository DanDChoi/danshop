package com.dan.danshop.domain.product.entity;

import com.dan.danshop.domain.product.dto.AddRequest;
import com.dan.danshop.domain.product.dto.UpdateRequest;
import com.dan.danshop.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product")
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productNo;
    private String productName;
    private BigDecimal price;
    private String category;
    private int stock;
    private String description;

    public static Product from(AddRequest addRequest) {
        return Product.builder()
                .productName(addRequest.getProductName())
                .price(addRequest.getPrice())
                .category(addRequest.getCategory())
                .stock(addRequest.getStock())
                .description(addRequest.getDescription())
                .build();
    }

    public void update(UpdateRequest updateRequest) {
        this.productName = updateRequest.getProductName();
        this.price = updateRequest.getPrice();
        this.category = updateRequest.getCategory();
        this.stock = updateRequest.getStock();
        this.description = updateRequest.getDescription();
    }


}
