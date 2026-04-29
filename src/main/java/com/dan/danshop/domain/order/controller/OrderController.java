package com.dan.danshop.domain.order.controller;

import com.dan.danshop.domain.order.dto.CreateRequest;
import com.dan.danshop.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody CreateRequest createRequest) {

        orderService.createOrder(createRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body("주문 생성 완료");
    }

    @PatchMapping("/orders/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.status(HttpStatus.OK).body("주문 취소 완료");
    }
}
