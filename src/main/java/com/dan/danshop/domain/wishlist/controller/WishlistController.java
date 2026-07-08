package com.dan.danshop.domain.wishlist.controller;

import com.dan.danshop.domain.wishlist.dto.WishlistResponse;
import com.dan.danshop.domain.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    private String userId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Void> add(@PathVariable Long productId) {
        wishlistService.add(userId(), productId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> remove(@PathVariable Long productId) {
        wishlistService.remove(userId(), productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<WishlistResponse>> getWishlist() {
        return ResponseEntity.ok(wishlistService.getWishlist(userId()));
    }

    @GetMapping("/{productId}/check")
    public ResponseEntity<Map<String, Boolean>> isWished(@PathVariable Long productId) {
        return ResponseEntity.ok(Map.of("wished", wishlistService.isWished(userId(), productId)));
    }

    @DeleteMapping
    public ResponseEntity<Void> clear() {
        wishlistService.clear(userId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/to-cart")
    public ResponseEntity<String> moveToCart(@PathVariable Long productId) {
        wishlistService.moveToCart(userId(), productId);
        return ResponseEntity.ok("장바구니에 추가되었습니다.");
    }
}
