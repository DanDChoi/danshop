package com.dan.danshop.domain.order.controller;

import com.dan.danshop.domain.order.dto.GuestOrderRequest;
import com.dan.danshop.domain.order.service.OrderService;
import com.dan.danshop.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/guest/orders")
@Tag(name = "Guest Order", description = "비회원 주문 API")
public class GuestOrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "비회원 주문 생성")
    @ApiResponse(responseCode = "201", description = "주문 생성 성공")
    @ApiResponse(responseCode = "409", description = "재고 부족",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Map<String, Long>> createGuestOrder(@Valid @RequestBody GuestOrderRequest request) {
        Long orderId = orderService.createGuestOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("orderId", orderId));
    }
}
