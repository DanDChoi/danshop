package com.dan.danshop.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SalesStatResponse {
    private BigDecimal totalSales;
    private Long orderCount;
    private LocalDateTime from;
    private LocalDateTime to;
}
