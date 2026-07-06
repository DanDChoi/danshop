package com.dan.danshop.domain.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddRequest {
    @NotBlank
    private String productName;

    @NotNull
    @Positive
    private BigDecimal price;

    private String category;

    @Min(0)
    private int stock;

    private String description;
}
