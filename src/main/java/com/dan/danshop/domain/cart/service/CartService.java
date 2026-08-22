package com.dan.danshop.domain.cart.service;

import com.dan.danshop.domain.cart.dto.CartCheckoutRequest;
import com.dan.danshop.domain.cart.dto.CartItem;
import com.dan.danshop.domain.cart.dto.CartResponse;
import com.dan.danshop.domain.order.dto.CreateRequest;
import com.dan.danshop.domain.order.dto.GuestOrderRequest;
import com.dan.danshop.domain.order.dto.OrderItemRequest;
import com.dan.danshop.domain.order.service.OrderService;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.repository.ProductRepository;
import com.dan.danshop.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.dan.danshop.global.exception.ErrorCode.*;

@Service
public class CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;
    private final OrderService orderService;

    public CartService(RedisTemplate<String, Object> redisTemplate,
                       ProductRepository productRepository,
                       @Lazy OrderService orderService) {
        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
        this.orderService = orderService;
    }

    private static final String CART_KEY_PREFIX = "cart:";
    public static final String GUEST_KEY_PREFIX = "guest:";

    private String cartKey(String userId) {
        return CART_KEY_PREFIX + userId;
    }

    public void addToCart(String userId, Long productId, int quantity) {
        productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));

        // 기존 수량에 quantity만큼 증가 (없으면 quantity로 초기화)
        redisTemplate.opsForHash().increment(cartKey(userId), productId.toString(), quantity);
    }

    public CartResponse getCart(String userId) {
        Map<Object, Object> cartMap = redisTemplate.opsForHash().entries(cartKey(userId));

        if (cartMap.isEmpty()) {
            return new CartResponse(List.of(), BigDecimal.ZERO);
        }

        List<CartItem> items = cartMap.entrySet().stream()
                .map(entry -> {
                    Long productId = Long.parseLong(entry.getKey().toString());
                    int qty = Integer.parseInt(entry.getValue().toString());
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
                    return new CartItem(
                            productId,
                            product.getProductName(),
                            product.getPrice(),
                            qty,
                            product.getPrice().multiply(BigDecimal.valueOf(qty))
                    );
                })
                .toList();

        BigDecimal totalAmount = items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(items, totalAmount);
    }

    public void updateQuantity(String userId, Long productId, int quantity) {
        String key = cartKey(userId);

        if (!redisTemplate.opsForHash().hasKey(key, productId.toString())) {
            throw new BusinessException(CART_ITEM_NOT_FOUND);
        }

        if (quantity <= 0) {
            redisTemplate.opsForHash().delete(key, productId.toString());
        } else {
            redisTemplate.opsForHash().put(key, productId.toString(), String.valueOf(quantity));
        }
    }

    public void removeFromCart(String userId, Long productId) {
        redisTemplate.opsForHash().delete(cartKey(userId), productId.toString());
    }

    public void clearCart(String userId) {
        redisTemplate.delete(cartKey(userId));
    }

    public Long checkout(String identifier, CartCheckoutRequest request) {
        CartResponse cart = getCart(identifier);
        if (cart.getItems().isEmpty()) {
            throw new BusinessException(CART_ITEM_NOT_FOUND);
        }

        List<OrderItemRequest> items = cart.getItems().stream()
                .map(item -> new OrderItemRequest(item.getProductId(), item.getQuantity()))
                .toList();

        Long orderId = identifier.startsWith(GUEST_KEY_PREFIX)
                ? orderService.createGuestOrder(toGuestOrderRequest(request, cart.getTotalAmount(), items))
                : orderService.createOrder(identifier, new CreateRequest(
                        request.getCouponId(),
                        request.getUsePoints(),
                        cart.getTotalAmount(),
                        request.getPostNo(),
                        request.getBaseAddr(),
                        request.getDetailAddr(),
                        items
                ));

        clearCart(identifier);
        return orderId;
    }

    private GuestOrderRequest toGuestOrderRequest(CartCheckoutRequest request, BigDecimal payAmount, List<OrderItemRequest> items) {
        if (isBlank(request.getOrdererName()) || isBlank(request.getOrdererEmail()) || isBlank(request.getOrdererPhone())) {
            throw new BusinessException(ORDERER_INFO_REQUIRED);
        }
        return new GuestOrderRequest(
                request.getOrdererName(),
                request.getOrdererEmail(),
                request.getOrdererPhone(),
                payAmount,
                request.getPostNo(),
                request.getBaseAddr(),
                request.getDetailAddr(),
                items
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
