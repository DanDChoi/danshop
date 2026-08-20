package com.dan.danshop.domain.cart.controller;

import com.dan.danshop.domain.cart.dto.CartCheckoutRequest;
import com.dan.danshop.domain.cart.dto.CartResponse;
import com.dan.danshop.domain.cart.service.CartService;
import com.dan.danshop.global.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.dan.danshop.global.exception.ErrorCode.GUEST_TOKEN_REQUIRED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
@Tag(name = "Cart", description = "장바구니 API")
public class CartController {

    private static final String GUEST_TOKEN_HEADER = "X-Guest-Token";
    private static final String GUEST_KEY_PREFIX = "guest:";

    private final CartService cartService;

    @PostMapping("/{productId}")
    @Operation(summary = "장바구니 상품 추가")
    public ResponseEntity<String> addToCart(@PathVariable Long productId,
                                            @RequestParam(defaultValue = "1") int quantity,
                                            @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken) {
        String cartKey = resolveCartKey(guestToken);
        cartService.addToCart(cartKey, productId, quantity);
        return ResponseEntity.ok("장바구니에 추가되었습니다.");
    }

    @GetMapping
    @Operation(summary = "장바구니 조회")
    public ResponseEntity<CartResponse> getCart(
            @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken) {
        String cartKey = resolveCartKey(guestToken);
        return ResponseEntity.ok(cartService.getCart(cartKey));
    }

    @PatchMapping("/{productId}")
    @Operation(summary = "장바구니 수량 변경")
    public ResponseEntity<String> updateQuantity(@PathVariable Long productId,
                                                 @RequestParam int quantity,
                                                 @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken) {
        String cartKey = resolveCartKey(guestToken);
        cartService.updateQuantity(cartKey, productId, quantity);
        return ResponseEntity.ok("수량이 변경되었습니다.");
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "장바구니 상품 삭제")
    public ResponseEntity<String> removeFromCart(@PathVariable Long productId,
                                                 @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken) {
        String cartKey = resolveCartKey(guestToken);
        cartService.removeFromCart(cartKey, productId);
        return ResponseEntity.ok("삭제되었습니다.");
    }

    @DeleteMapping
    @Operation(summary = "장바구니 전체 비우기")
    public ResponseEntity<String> clearCart(
            @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken) {
        String cartKey = resolveCartKey(guestToken);
        cartService.clearCart(cartKey);
        return ResponseEntity.ok("장바구니가 비워졌습니다.");
    }

    @PostMapping("/checkout")
    @Operation(summary = "장바구니 바로 주문")
    public ResponseEntity<Map<String, Long>> checkout(@Valid @RequestBody CartCheckoutRequest request,
                                                       @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken) {
        String cartKey = resolveCartKey(guestToken);
        Long orderId = cartService.checkout(cartKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("orderId", orderId));
    }

    /**
     * 회원은 SecurityContext의 userId를, 비회원은 클라이언트가 생성한 X-Guest-Token을
     * cart key로 사용한다. "guest:" 접두사로 회원 userId와의 충돌을 방지한다.
     */
    private String resolveCartKey(String guestToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }
        if (guestToken == null || guestToken.isBlank()) {
            throw new BusinessException(GUEST_TOKEN_REQUIRED);
        }
        return GUEST_KEY_PREFIX + guestToken;
    }
}
