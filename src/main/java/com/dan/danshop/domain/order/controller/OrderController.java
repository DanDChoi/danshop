package com.dan.danshop.domain.order.controller;

import com.dan.danshop.domain.order.dto.CreateRequest;
import com.dan.danshop.domain.order.dto.OrderDetailResponse;
import com.dan.danshop.domain.order.dto.OrderResponse;
import com.dan.danshop.domain.order.dto.UpdateAddressRequest;
import com.dan.danshop.domain.order.service.OrderService;
import com.dan.danshop.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @ApiResponse(responseCode = "201", description = "주문 생성 성공")
    @ApiResponse(responseCode = "409", description = "재고 부족",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateRequest createRequest) {

        orderService.createOrder(createRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body("주문 생성 완료");
    }

    @PatchMapping("/orders/{id}/cancel")
    @Operation(summary = "주문 취소")
    @ApiResponse(responseCode = "200", description = "취소 성공")
    @ApiResponse(responseCode = "400", description = "취소 불가 상태",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "본인 주문 아님",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.status(HttpStatus.OK).body("주문 취소 완료");
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "주문 단건 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "주문 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<OrderDetailResponse> findOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findOrder(id));
    }

    @PatchMapping("/orders/{id}/address")
    @Operation(summary = "배송지 변경 (PENDING 상태만 가능)")
    @ApiResponse(responseCode = "200", description = "변경 성공")
    @ApiResponse(responseCode = "400", description = "배송 준비 전 주문 아님",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<String> updateAddress(@PathVariable Long id,
                                                @Valid @RequestBody UpdateAddressRequest request) {
        orderService.updateAddress(id, request);
        return ResponseEntity.ok("배송지가 변경되었습니다.");
    }

    @GetMapping("/orders")
    @Operation(summary = "주문 목록 조회")
    public ResponseEntity<Page<OrderResponse>> findOrderList(@RequestParam(required = false, defaultValue = "0") int page,
                                                             @RequestParam(required = false, defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.findOrderList(page, size));
    }
}
