package com.dan.danshop.domain.admin.controller;

import com.dan.danshop.domain.admin.dto.OrderStatusStatResponse;
import com.dan.danshop.domain.admin.dto.ProductSalesResponse;
import com.dan.danshop.domain.admin.dto.SalesStatResponse;
import com.dan.danshop.domain.admin.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/sales")
    public ResponseEntity<SalesStatResponse> getSalesStat(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(adminStatsService.getSalesStat(from, to));
    }

    @GetMapping("/products/top")
    public ResponseEntity<List<ProductSalesResponse>> getTopProductsBySales(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(adminStatsService.getTopProductsBySales(limit));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderStatusStatResponse>> getOrderStatusStat() {
        return ResponseEntity.ok(adminStatsService.getOrderStatusStat());
    }
}
