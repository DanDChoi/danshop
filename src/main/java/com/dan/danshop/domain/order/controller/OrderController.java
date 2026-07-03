package com.dan.danshop.domain.order.controller;

import com.dan.danshop.domain.order.dto.CreateRequest;
import com.dan.danshop.domain.order.dto.OrderResponse;
import com.dan.danshop.domain.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Order", description = "주문 관련 API")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    @Operation(summary = "주문 생성")
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateRequest createRequest) {

        orderService.createOrder(createRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body("주문 생성 완료");
    }

    @PatchMapping("/orders/{id}/cancel")
    @Operation(summary = "주문 취소")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.status(HttpStatus.OK).body("주문 취소 완료");
    }

    @GetMapping("/orders")
    @Operation(summary = "주문 목록 조회")
    public ResponseEntity<Page<OrderResponse>> findOrderList (@RequestParam(required = false, defaultValue = "0") int page,
                                                              @RequestParam(required = false, defaultValue = "10") int size
                                                              ) {
        return ResponseEntity.ok(orderService.findOrderList(page, size));
    }
}
