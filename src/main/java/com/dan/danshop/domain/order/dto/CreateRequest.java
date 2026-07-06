package com.dan.danshop.domain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRequest {
    private Long couponId;
    private Long usePoints;

    @NotNull
    @Positive
    private BigDecimal payAmount;

    @NotBlank
    private String postNo;

    @NotBlank
    private String baseAddr;

    @NotBlank
    private String detailAddr;

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;
}
