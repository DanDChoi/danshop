package com.dan.danshop.domain.order.controller;

import com.dan.danshop.domain.order.dto.CreateRequest;
import com.dan.danshop.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/order")
    public ResponseEntity<?> createOrder(@RequestBody CreateRequest createRequest) {

        orderService.createOrder(createRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body("주문 생성 완료");
    }
}
