package com.dan.danshop.domain.wishlist.service;

import com.dan.danshop.domain.cart.service.CartService;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.repository.ProductRepository;
import com.dan.danshop.domain.wishlist.dto.WishlistResponse;
import com.dan.danshop.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.dan.danshop.global.exception.ErrorCode.PRODUCT_NOT_FOUND;

@Service
public class WishlistService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;
    private final CartService cartService;

    @Autowired
    public WishlistService(RedisTemplate<String, Object> redisTemplate,
                           ProductRepository productRepository,
                           @Lazy CartService cartService) {
        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    private static final String WISHLIST_PREFIX = "wishlist:";

    private String key(String userId) {
        return WISHLIST_PREFIX + userId;
    }

    public void add(String userId, Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
        redisTemplate.opsForSet().add(key(userId), productId.toString());
    }

    public void remove(String userId, Long productId) {
        redisTemplate.opsForSet().remove(key(userId), productId.toString());
    }

    public List<WishlistResponse> getWishlist(String userId) {
        Set<Object> members = redisTemplate.opsForSet().members(key(userId));
        if (members == null || members.isEmpty()) return Collections.emptyList();

        return members.stream()
                .map(m -> {
                    Long productId = Long.parseLong(m.toString());
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
                    return new WishlistResponse(
                            product.getId(),
                            product.getProductName(),
                            product.getPrice(),
                            product.getCategory(),
                            product.getAvgRating()
                    );
                })
                .toList();
    }

    public boolean isWished(String userId, Long productId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key(userId), productId.toString()));
    }

    public void clear(String userId) {
        redisTemplate.delete(key(userId));
    }

    public void moveToCart(String userId, Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
        cartService.addToCart(userId, productId, 1);
        remove(userId, productId);
    }
}
