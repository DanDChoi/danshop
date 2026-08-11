package com.dan.danshop.domain.admin.service;

import com.dan.danshop.domain.admin.dto.OrderStatusStatResponse;
import com.dan.danshop.domain.admin.dto.ProductSalesResponse;
import com.dan.danshop.domain.admin.dto.SalesStatResponse;
import com.dan.danshop.domain.order.dto.CreateRequest;
import com.dan.danshop.domain.order.dto.OrderItemRequest;
import com.dan.danshop.domain.order.entity.Order;
import com.dan.danshop.domain.order.entity.OrderStatus;
import com.dan.danshop.domain.coupon.repository.UserCouponRepository;
import com.dan.danshop.domain.order.repository.OrderItemRepository;
import com.dan.danshop.domain.order.repository.OrderRepository;
import com.dan.danshop.domain.order.service.OrderService;
import com.dan.danshop.domain.point.repository.PointHistoryRepository;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.repository.ProductRepository;
import com.dan.danshop.domain.review.repository.ReviewRepository;
import com.dan.danshop.domain.user.entity.Role;
import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AdminStatsServiceTest {

    @Autowired private AdminStatsService adminStatsService;
    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private PointHistoryRepository pointHistoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private UserCouponRepository userCouponRepository;

    private User user;
    private Product productA;
    private Product productB;

    @BeforeEach
    void setUp() {
        pointHistoryRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        reviewRepository.deleteAll();
        userCouponRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .userId("statuser")
                .email("stat@test.com")
                .password("password")
                .name("통계유저")
                .role(Role.ROLE_USER)
                .build());

        productA = productRepository.save(Product.builder()
                .productName("상품A")
                .price(BigDecimal.valueOf(10000))
                .stock(100)
                .build());

        productB = productRepository.save(Product.builder()
                .productName("상품B")
                .price(BigDecimal.valueOf(20000))
                .stock(100)
                .build());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getUserId(), null, List.of())
        );
    }

    private Long placeOrder(Product product, int quantity) {
        return orderService.createOrder(new CreateRequest(
                null, null,
                product.getPrice().multiply(BigDecimal.valueOf(quantity)),
                "12345", "서울시", "101호",
                List.of(new OrderItemRequest(product.getId(), quantity))
        ));
    }

    private void markAsPaid(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        ReflectionTestUtils.setField(order, "status", OrderStatus.PAID);
        orderRepository.save(order);
    }

    @Test
    void PAID_상태_주문만_매출에_집계된다() {
        // given - PAID 2건, PENDING 1건
        Long orderId1 = placeOrder(productA, 1); // 10000
        Long orderId2 = placeOrder(productA, 2); // 20000
        placeOrder(productB, 1);                 // PENDING - 집계 제외

        markAsPaid(orderId1);
        markAsPaid(orderId2);

        // when
        SalesStatResponse stat = adminStatsService.getSalesStat(null, null);

        // then
        assertThat(stat.getOrderCount()).isEqualTo(2L);
        assertThat(stat.getTotalSales()).isEqualByComparingTo(BigDecimal.valueOf(30000));

        System.out.println("총 매출: " + stat.getTotalSales() + ", 주문 수: " + stat.getOrderCount());
    }

    @Test
    void 기간_필터를_적용하면_해당_기간_매출만_조회된다() {
        // given
        Long orderId = placeOrder(productA, 1);
        markAsPaid(orderId);

        LocalDateTime from = LocalDateTime.now().minusHours(1);
        LocalDateTime to = LocalDateTime.now().plusHours(1);

        // when
        SalesStatResponse inRange = adminStatsService.getSalesStat(from, to);
        SalesStatResponse outOfRange = adminStatsService.getSalesStat(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        // then
        assertThat(inRange.getOrderCount()).isEqualTo(1L);
        assertThat(outOfRange.getOrderCount()).isEqualTo(0L);

        System.out.println("기간 내 매출: " + inRange.getTotalSales() + ", 기간 외 주문 수: " + outOfRange.getOrderCount());
    }

    @Test
    void 취소된_주문은_상품별_판매량에서_제외된다() {
        // given - 상품A 3개 주문 후 취소, 상품B 2개 정상 주문
        Long cancelOrderId = placeOrder(productA, 3);
        orderService.cancelOrder(user.getUserId(), cancelOrderId);
        placeOrder(productB, 2);

        // when
        List<ProductSalesResponse> top = adminStatsService.getTopProductsBySales(10);

        // then - 취소된 상품A는 집계 제외, 상품B만 존재
        assertThat(top).hasSize(1);
        assertThat(top.get(0).getProductName()).isEqualTo("상품B");
        assertThat(top.get(0).getTotalQuantity()).isEqualTo(2);

        System.out.println("TOP 상품: " + top.get(0).getProductName() + " - " + top.get(0).getTotalQuantity() + "개");
    }

    @Test
    void 상품별_판매량이_내림차순으로_정렬된다() {
        // given - 상품A 5개, 상품B 2개 주문
        placeOrder(productA, 5);
        placeOrder(productB, 2);

        // when
        List<ProductSalesResponse> top = adminStatsService.getTopProductsBySales(10);

        // then - 상품A(5개)가 상품B(2개)보다 상위
        assertThat(top.get(0).getProductName()).isEqualTo("상품A");
        assertThat(top.get(0).getTotalQuantity()).isEqualTo(5);
        assertThat(top.get(1).getProductName()).isEqualTo("상품B");

        System.out.println("1위: " + top.get(0).getProductName() + "(" + top.get(0).getTotalQuantity() + "개)"
                + ", 2위: " + top.get(1).getProductName() + "(" + top.get(1).getTotalQuantity() + "개)");
    }

    @Test
    void 주문_상태별_현황이_정확히_집계된다() {
        // given - PENDING 2건, CANCELLED 1건
        placeOrder(productA, 1);
        placeOrder(productB, 1);
        Long cancelId = placeOrder(productA, 1);
        orderService.cancelOrder(user.getUserId(), cancelId);

        // when
        List<OrderStatusStatResponse> stats = adminStatsService.getOrderStatusStat();

        // then
        long pendingCount = stats.stream()
                .filter(s -> s.getStatus() == OrderStatus.PENDING)
                .mapToLong(OrderStatusStatResponse::getCount).sum();
        long cancelledCount = stats.stream()
                .filter(s -> s.getStatus() == OrderStatus.CANCELLED)
                .mapToLong(OrderStatusStatResponse::getCount).sum();

        assertThat(pendingCount).isEqualTo(2L);
        assertThat(cancelledCount).isEqualTo(1L);

        stats.forEach(s -> System.out.println(s.getStatus() + ": " + s.getCount() + "건"));
    }

    @Test
    void limit_파라미터로_TOP_N_상품_수를_제한할_수_있다() {
        // given - 상품A, 상품B 각각 주문
        placeOrder(productA, 3);
        placeOrder(productB, 1);

        // when - limit=1
        List<ProductSalesResponse> top1 = adminStatsService.getTopProductsBySales(1);

        // then
        assertThat(top1).hasSize(1);
        assertThat(top1.get(0).getProductName()).isEqualTo("상품A");

        System.out.println("TOP 1 상품: " + top1.get(0).getProductName());
    }
}
