package com.dan.danshop.domain.admin.controller;

import com.dan.danshop.domain.admin.dto.OrderStatusUpdateRequest;
import com.dan.danshop.domain.admin.service.AdminOrderService;
import com.dan.danshop.domain.order.dto.OrderResponse;
import com.dan.danshop.domain.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Order", description = "어드민 주문 관리 API")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    @Operation(summary = "전체 주문 목록 조회 (ADMIN)")
    public ResponseEntity<Page<OrderResponse>> findAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status) {
        return ResponseEntity.ok(adminOrderService.findAllOrders(page, size, status));
    }

    @PatchMapping("/{orderId}/status")
    @Operation(summary = "주문 상태 변경 (ADMIN)")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        adminOrderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok("주문 상태가 변경되었습니다: " + request.getStatus().getDescription());
    }
}
