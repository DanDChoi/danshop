package com.dan.danshop.domain.cart.service;

import com.dan.danshop.domain.cart.dto.CartItem;
import com.dan.danshop.domain.cart.dto.CartResponse;
import com.dan.danshop.domain.order.repository.OrderItemRepository;
import com.dan.danshop.domain.order.repository.OrderRepository;
import com.dan.danshop.domain.point.repository.PointHistoryRepository;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.repository.ProductRepository;
import com.dan.danshop.domain.review.repository.ReviewRepository;
import com.dan.danshop.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class CartServiceTest {

    @Autowired private CartService cartService;
    @Autowired private ProductRepository productRepository;
    @Autowired private RedisTemplate<String, Object> redisTemplate;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PointHistoryRepository pointHistoryRepository;

    private static final String USER_ID = "cartuser";

    private Product productA;
    private Product productB;

    @BeforeEach
    void setUp() {
        redisTemplate.delete("cart:" + USER_ID);
        pointHistoryRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        reviewRepository.deleteAll();
        productRepository.deleteAll();

        productA = productRepository.save(Product.builder()
                .productName("상품A").price(BigDecimal.valueOf(10000)).stock(10).build());
        productB = productRepository.save(Product.builder()
                .productName("상품B").price(BigDecimal.valueOf(20000)).stock(5).build());
    }

    @Test
    void 상품을_장바구니에_추가할_수_있다() {
        // when
        cartService.addToCart(USER_ID, productA.getId(), 2);

        // then
        CartResponse cart = cartService.getCart(USER_ID);
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
        System.out.println("장바구니 추가: " + cart.getItems().get(0).getProductName() + " x" + cart.getItems().get(0).getQuantity());
    }

    @Test
    void 동일_상품을_여러_번_추가하면_수량이_누적된다() {
        // when
        cartService.addToCart(USER_ID, productA.getId(), 2);
        cartService.addToCart(USER_ID, productA.getId(), 3);

        // then
        CartResponse cart = cartService.getCart(USER_ID);
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
        System.out.println("누적 수량: " + cart.getItems().get(0).getQuantity());
    }

    @Test
    void 여러_상품을_장바구니에_담을_수_있다() {
        // when
        cartService.addToCart(USER_ID, productA.getId(), 1);
        cartService.addToCart(USER_ID, productB.getId(), 2);

        // then
        CartResponse cart = cartService.getCart(USER_ID);
        assertThat(cart.getItems()).hasSize(2);

        BigDecimal expectedTotal = BigDecimal.valueOf(10000).add(BigDecimal.valueOf(40000));
        assertThat(cart.getTotalAmount()).isEqualByComparingTo(expectedTotal);
        System.out.println("총 금액: " + cart.getTotalAmount());
    }

    @Test
    void 빈_장바구니_조회시_빈_목록과_0원이_반환된다() {
        // when
        CartResponse cart = cartService.getCart(USER_ID);

        // then
        assertThat(cart.getItems()).isEmpty();
        assertThat(cart.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        System.out.println("빈 장바구니 totalAmount: " + cart.getTotalAmount());
    }

    @Test
    void 수량을_변경할_수_있다() {
        // given
        cartService.addToCart(USER_ID, productA.getId(), 3);

        // when
        cartService.updateQuantity(USER_ID, productA.getId(), 1);

        // then
        CartResponse cart = cartService.getCart(USER_ID);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(1);
        System.out.println("수량 변경 후: " + cart.getItems().get(0).getQuantity());
    }

    @Test
    void 수량을_0으로_변경하면_장바구니에서_삭제된다() {
        // given
        cartService.addToCart(USER_ID, productA.getId(), 2);

        // when
        cartService.updateQuantity(USER_ID, productA.getId(), 0);

        // then
        CartResponse cart = cartService.getCart(USER_ID);
        assertThat(cart.getItems()).isEmpty();
        System.out.println("수량 0 설정 후 장바구니 비어있음 확인");
    }

    @Test
    void 장바구니에_없는_상품_수량_변경시_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> cartService.updateQuantity(USER_ID, productA.getId(), 2))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("장바구니에 존재하지 않는 상품입니다.");
        System.out.println("미담긴 상품 수량 변경 예외 확인");
    }

    @Test
    void 상품을_장바구니에서_삭제할_수_있다() {
        // given
        cartService.addToCart(USER_ID, productA.getId(), 2);
        cartService.addToCart(USER_ID, productB.getId(), 1);

        // when
        cartService.removeFromCart(USER_ID, productA.getId());

        // then
        CartResponse cart = cartService.getCart(USER_ID);
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getProductName()).isEqualTo("상품B");
        System.out.println("삭제 후 남은 상품: " + cart.getItems().get(0).getProductName());
    }

    @Test
    void 장바구니_전체를_비울_수_있다() {
        // given
        cartService.addToCart(USER_ID, productA.getId(), 1);
        cartService.addToCart(USER_ID, productB.getId(), 1);

        // when
        cartService.clearCart(USER_ID);

        // then
        CartResponse cart = cartService.getCart(USER_ID);
        assertThat(cart.getItems()).isEmpty();
        System.out.println("전체 비우기 후 장바구니 크기: 0");
    }

    @Test
    void 존재하지_않는_상품을_장바구니에_추가하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> cartService.addToCart(USER_ID, 99999L, 1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다.");
        System.out.println("존재하지 않는 상품 추가 예외 확인");
    }

    @Test
    void 장바구니_합계금액이_올바르게_계산된다() {
        // given - 10000 * 2 + 20000 * 3 = 80000
        cartService.addToCart(USER_ID, productA.getId(), 2);
        cartService.addToCart(USER_ID, productB.getId(), 3);

        // when
        CartResponse cart = cartService.getCart(USER_ID);

        // then
        assertThat(cart.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(80000));

        for (CartItem item : cart.getItems()) {
            BigDecimal expected = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            assertThat(item.getTotalPrice()).isEqualByComparingTo(expected);
            System.out.println(item.getProductName() + " 단가계산: " + item.getTotalPrice() + "원");
        }
    }
}
