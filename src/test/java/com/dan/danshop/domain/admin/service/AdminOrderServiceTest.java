package com.dan.danshop.domain.admin.service;

import com.dan.danshop.domain.admin.dto.OrderStatusUpdateRequest;
import com.dan.danshop.domain.coupon.repository.UserCouponRepository;
import com.dan.danshop.domain.order.dto.CreateRequest;
import com.dan.danshop.domain.order.dto.OrderItemRequest;
import com.dan.danshop.domain.order.dto.OrderResponse;
import com.dan.danshop.domain.order.entity.Order;
import com.dan.danshop.domain.order.entity.OrderStatus;
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
import com.dan.danshop.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class AdminOrderServiceTest {

    @Autowired private AdminOrderService adminOrderService;
    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private PointHistoryRepository pointHistoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private UserCouponRepository userCouponRepository;

    private User user;
    private Product product;

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
                .userId("admintest")
                .email("admin@test.com")
                .password("password")
                .name("어드민테스트유저")
                .role(Role.ROLE_USER)
                .build());

        product = productRepository.save(Product.builder()
                .productName("테스트상품")
                .price(BigDecimal.valueOf(10000))
                .stock(100)
                .build());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getUserId(), null, List.of())
        );
    }

    private Long placeOrder() {
        return orderService.createOrder(new CreateRequest(
                null, null,
                BigDecimal.valueOf(10000),
                "12345", "서울시", "101호",
                List.of(new OrderItemRequest(product.getId(), 1))
        ));
    }

    @Test
    void 주문_상태를_PENDING에서_PAID로_변경할_수_있다() {
        // given
        Long orderId = placeOrder();
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        ReflectionTestUtils.setField(request, "status", OrderStatus.PAID);

        // when
        adminOrderService.updateOrderStatus(orderId, request);

        // then
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);

        System.out.println("주문 #" + orderId + " 상태 변경: PENDING → PAID");
    }

    @Test
    void 허용되지_않는_상태_전환시_예외가_발생한다() {
        // given - PENDING 상태에서 SHIPPED로 직접 변경 시도
        Long orderId = placeOrder();
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        ReflectionTestUtils.setField(request, "status", OrderStatus.SHIPPED);

        // then
        assertThatThrownBy(() -> adminOrderService.updateOrderStatus(orderId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("허용되지 않는 주문 상태 변경입니다.");

        System.out.println("PENDING → SHIPPED 직접 전환 예외 확인");
    }

    @Test
    void 전체_주문_목록을_페이지네이션으로_조회할_수_있다() {
        // given - 주문 3건 생성
        placeOrder();
        placeOrder();
        placeOrder();

        // when
        Page<OrderResponse> result = adminOrderService.findAllOrders(0, 10, null);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3L);

        System.out.println("전체 주문 수: " + result.getTotalElements());
    }

    @Test
    void 상태_필터로_특정_상태_주문만_조회된다() {
        // given - 주문 2건 생성 후 1건만 PAID로 변경
        Long orderId1 = placeOrder();
        placeOrder();

        OrderStatusUpdateRequest paidReq = new OrderStatusUpdateRequest();
        ReflectionTestUtils.setField(paidReq, "status", OrderStatus.PAID);
        adminOrderService.updateOrderStatus(orderId1, paidReq);

        // when
        Page<OrderResponse> paidOrders = adminOrderService.findAllOrders(0, 10, OrderStatus.PAID);
        Page<OrderResponse> pendingOrders = adminOrderService.findAllOrders(0, 10, OrderStatus.PENDING);

        // then
        assertThat(paidOrders.getTotalElements()).isEqualTo(1L);
        assertThat(pendingOrders.getTotalElements()).isEqualTo(1L);

        System.out.println("PAID: " + paidOrders.getTotalElements() + "건, PENDING: " + pendingOrders.getTotalElements() + "건");
    }

    @Test
    void PAID에서_SHIPPED_SHIPPED에서_DELIVERED로_순차_변경된다() {
        // given
        Long orderId = placeOrder();

        OrderStatusUpdateRequest paidReq = new OrderStatusUpdateRequest();
        ReflectionTestUtils.setField(paidReq, "status", OrderStatus.PAID);

        OrderStatusUpdateRequest shippedReq = new OrderStatusUpdateRequest();
        ReflectionTestUtils.setField(shippedReq, "status", OrderStatus.SHIPPED);

        OrderStatusUpdateRequest deliveredReq = new OrderStatusUpdateRequest();
        ReflectionTestUtils.setField(deliveredReq, "status", OrderStatus.DELIVERED);

        // when
        adminOrderService.updateOrderStatus(orderId, paidReq);
        adminOrderService.updateOrderStatus(orderId, shippedReq);
        adminOrderService.updateOrderStatus(orderId, deliveredReq);

        // then
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);

        System.out.println("주문 #" + orderId + " 최종 상태: " + order.getStatus());
    }
}
