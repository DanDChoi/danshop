package com.dan.danshop.domain.order.service;

import com.dan.danshop.domain.coupon.entity.Coupon;
import com.dan.danshop.domain.coupon.entity.DiscountType;
import com.dan.danshop.domain.coupon.repository.CouponRepository;
import com.dan.danshop.domain.coupon.repository.UserCouponRepository;
import com.dan.danshop.domain.coupon.service.CouponService;
import com.dan.danshop.domain.order.dto.CreateRequest;
import com.dan.danshop.domain.order.dto.OrderItemRequest;
import com.dan.danshop.domain.order.entity.Order;
import com.dan.danshop.domain.order.repository.OrderItemRepository;
import com.dan.danshop.domain.order.repository.OrderRepository;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.repository.ProductRepository;
import com.dan.danshop.domain.user.entity.Role;
import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.domain.user.repository.UserRepository;
import com.dan.danshop.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class OrderCouponServiceTest {

    @Autowired private OrderService orderService;
    @Autowired private CouponService couponService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CouponRepository couponRepository;
    @Autowired private UserCouponRepository userCouponRepository;

    // ───────────────────────────────────────────
    // 4단계: 주문 시 쿠폰 적용 검증
    // - 금액할인 / 비율할인 / 최소금액 미충족 / 이미 사용 / 만료
    // ───────────────────────────────────────────

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        userCouponRepository.deleteAll();
        couponRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .userId("testuser")
                .email("test@test.com")
                .password("password")
                .name("테스트유저")
                .role(Role.ROLE_USER)
                .build());

        product = productRepository.save(Product.builder()
                .productName("테스트상품")
                .price(BigDecimal.valueOf(50000))
                .stock(100)
                .build());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getUserId(), null, List.of())
        );
    }

    @Test
    void 금액할인_쿠폰_적용시_할인금액이_차감된다() {
        // given - 5000원 할인 쿠폰 생성 및 발급
        Coupon coupon = Coupon.builder()
                .name("5000원 할인")
                .discountType(DiscountType.AMOUNT)
                .discountValue(BigDecimal.valueOf(5000))
                .minOrderAmount(BigDecimal.valueOf(30000))
                .totalQuantity(10)
                .remainQuantity(10)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        couponService.createCoupon(coupon);
        couponService.issueCoupon(coupon.getId(), user.getUserId());

        // when - 50000원 주문에 5000원 쿠폰 적용
        CreateRequest request = new CreateRequest(
                coupon.getId(),
                null,
                BigDecimal.valueOf(50000),
                "12345", "서울시 강남구", "101호",
                List.of(new OrderItemRequest(product.getId(), 1))
        );
        Long orderId = orderService.createOrder(request);

        // then - 실결제금액 45000원
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getPayAmount()).isEqualByComparingTo(BigDecimal.valueOf(45000));

        System.out.println("원래금액: 50000, 할인: -5000, 실결제: " + order.getPayAmount());
    }

    @Test
    void 비율할인_쿠폰_적용시_할인율이_적용된다() {
        // given - 10% 할인 쿠폰 생성 및 발급
        Coupon coupon = Coupon.builder()
                .name("10% 할인")
                .discountType(DiscountType.RATE)
                .discountValue(BigDecimal.valueOf(10))
                .minOrderAmount(BigDecimal.ZERO)
                .totalQuantity(10)
                .remainQuantity(10)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        couponService.createCoupon(coupon);
        couponService.issueCoupon(coupon.getId(), user.getUserId());

        // when - 50000원 주문에 10% 할인 적용
        CreateRequest request = new CreateRequest(
                coupon.getId(),
                null,
                BigDecimal.valueOf(50000),
                "12345", "서울시 강남구", "101호",
                List.of(new OrderItemRequest(product.getId(), 1))
        );
        Long orderId = orderService.createOrder(request);

        // then - 실결제금액 45000원 (50000 * 0.9)
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getPayAmount()).isEqualByComparingTo(BigDecimal.valueOf(45000));

        System.out.println("원래금액: 50000, 10% 할인 적용, 실결제: " + order.getPayAmount());
    }

    @Test
    void 최소주문금액_미충족시_예외가_발생한다() {
        // given - 최소 주문금액 30000원 쿠폰
        Coupon coupon = Coupon.builder()
                .name("30000원 이상 할인")
                .discountType(DiscountType.AMOUNT)
                .discountValue(BigDecimal.valueOf(5000))
                .minOrderAmount(BigDecimal.valueOf(30000))
                .totalQuantity(10)
                .remainQuantity(10)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        couponService.createCoupon(coupon);
        couponService.issueCoupon(coupon.getId(), user.getUserId());

        // when - 20000원 주문 시도 (최소 금액 미달)
        CreateRequest request = new CreateRequest(
                coupon.getId(),
                null,
                BigDecimal.valueOf(20000),
                "12345", "서울시 강남구", "101호",
                List.of(new OrderItemRequest(product.getId(), 1))
        );

        // then - 최소 주문금액 미충족 예외
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("최소 주문금액을 충족하지 않습니다.");

        System.out.println("최소 주문금액(30000) 미충족 예외 확인 완료");
    }

    @Test
    void 이미_사용된_쿠폰으로_주문시_예외가_발생한다() {
        // given - 쿠폰 발급 후 첫 번째 주문으로 사용
        Coupon coupon = Coupon.builder()
                .name("일회용 쿠폰")
                .discountType(DiscountType.AMOUNT)
                .discountValue(BigDecimal.valueOf(5000))
                .minOrderAmount(BigDecimal.ZERO)
                .totalQuantity(10)
                .remainQuantity(10)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        couponService.createCoupon(coupon);
        couponService.issueCoupon(coupon.getId(), user.getUserId());

        CreateRequest firstRequest = new CreateRequest(
                coupon.getId(),
                null,
                BigDecimal.valueOf(50000),
                "12345", "서울시 강남구", "101호",
                List.of(new OrderItemRequest(product.getId(), 1))
        );
        orderService.createOrder(firstRequest);

        // when - 동일 쿠폰으로 두 번째 주문 시도
        CreateRequest secondRequest = new CreateRequest(
                coupon.getId(),
                null,
                BigDecimal.valueOf(50000),
                "12345", "서울시 강남구", "101호",
                List.of(new OrderItemRequest(product.getId(), 1))
        );

        // then - 이미 사용된 쿠폰 예외
        assertThatThrownBy(() -> orderService.createOrder(secondRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 사용된 쿠폰입니다.");

        System.out.println("이미 사용된 쿠폰 예외 확인 완료");
    }

    @Test
    void 만료된_쿠폰으로_주문시_예외가_발생한다() {
        // given - 어제 만료된 쿠폰 생성 및 발급 (issueCoupon은 만료 체크 없음)
        Coupon coupon = Coupon.builder()
                .name("만료 쿠폰")
                .discountType(DiscountType.AMOUNT)
                .discountValue(BigDecimal.valueOf(5000))
                .minOrderAmount(BigDecimal.ZERO)
                .totalQuantity(10)
                .remainQuantity(10)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();
        couponService.createCoupon(coupon);
        couponService.issueCoupon(coupon.getId(), user.getUserId());

        // when - 만료된 쿠폰으로 주문 시도
        CreateRequest request = new CreateRequest(
                coupon.getId(),
                null,
                BigDecimal.valueOf(50000),
                "12345", "서울시 강남구", "101호",
                List.of(new OrderItemRequest(product.getId(), 1))
        );

        // then - 만료 쿠폰 예외
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("만료된 쿠폰입니다.");

        System.out.println("만료 쿠폰 예외 확인 완료");
    }
}
