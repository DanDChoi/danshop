package com.dan.danshop.domain.cart.controller;

import com.dan.danshop.domain.cart.dto.CartResponse;
import com.dan.danshop.domain.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
@Tag(name = "Cart", description = "장바구니 API")
public class CartController {

    private final CartService cartService;

    @PostMapping("/{productId}")
    @Operation(summary = "장바구니 상품 추가")
    public ResponseEntity<String> addToCart(@PathVariable Long productId,
                                            @RequestParam(defaultValue = "1") int quantity) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.addToCart(userId, productId, quantity);
        return ResponseEntity.ok("장바구니에 추가되었습니다.");
    }

    @GetMapping
    @Operation(summary = "장바구니 조회")
    public ResponseEntity<CartResponse> getCart() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PatchMapping("/{productId}")
    @Operation(summary = "장바구니 수량 변경")
    public ResponseEntity<String> updateQuantity(@PathVariable Long productId,
                                                 @RequestParam int quantity) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.updateQuantity(userId, productId, quantity);
        return ResponseEntity.ok("수량이 변경되었습니다.");
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "장바구니 상품 삭제")
    public ResponseEntity<String> removeFromCart(@PathVariable Long productId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.removeFromCart(userId, productId);
        return ResponseEntity.ok("삭제되었습니다.");
    }

    @DeleteMapping
    @Operation(summary = "장바구니 전체 비우기")
    public ResponseEntity<String> clearCart() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.clearCart(userId);
        return ResponseEntity.ok("장바구니가 비워졌습니다.");
    }
}
