package com.dan.danshop.domain.point.service;

import com.dan.danshop.domain.order.dto.CreateRequest;
import com.dan.danshop.domain.order.dto.OrderItemRequest;
import com.dan.danshop.domain.coupon.repository.UserCouponRepository;
import com.dan.danshop.domain.order.repository.OrderItemRepository;
import com.dan.danshop.domain.order.repository.OrderRepository;
import com.dan.danshop.domain.order.service.OrderService;
import com.dan.danshop.domain.point.entity.PointHistory;
import com.dan.danshop.domain.point.entity.PointType;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class OrderPointServiceTest {

    @Autowired private OrderService orderService;
    @Autowired private PointService pointService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private PointHistoryRepository pointHistoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
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
                .userId("pointuser")
                .email("point@test.com")
                .password("password")
                .name("포인트테스트유저")
                .role(Role.ROLE_USER)
                .build());

        product = productRepository.save(Product.builder()
                .productName("포인트테스트상품")
                .price(BigDecimal.valueOf(50000))
                .stock(100)
                .build());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getUserId(), null, List.of())
        );
    }

    @Test
    void 주문_완료시_결제금액의_1퍼센트가_적립된다() {
        // given - 50000원 주문
        CreateRequest request = new CreateRequest(
                null, null,
                BigDecimal.valueOf(50000),
                "12345", "서울시 강남구", "101호",
                List.of(new OrderItemRequest(product.getId(), 1))
        );

        // when
        Long orderId = orderService.createOrder(user.getUserId(), request);

        // then - 500포인트(1%) 적립
        long balance = pointService.getBalance(user.getUserId());
        assertThat(balance).isEqualTo(500L);

        List<PointHistory> histories = pointHistoryRepository.findByUserOrderByCreatedAtDesc(
                userRepository.findByUserId(user.getUserId()).orElseThrow()
        );
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getType()).isEqualTo(PointType.EARN);
        assertThat(histories.get(0).getAmount()).isEqualTo(500L);
        assertThat(histories.get(0).getOrderId()).isEqualTo(orderId);

        System.out.println("결제금액: 50000, 적립포인트: " + balance);
    }

    @Test
    void 포인트_사용시_결제금액에서_차감된다() {
        // given - 유저에게 1000포인트 미리 부여
        user = userRepository.findByUserId(user.getUserId()).orElseThrow();
        user.addPoints(1000L);
        userRepository.save(user);

        // when - 50000원 주문 시 1000포인트 사용
        CreateRequest request = new CreateRequest(
                null, 1000L,
                BigDecimal.valueOf(50000),
                "12345", "서울시 강남구", "101호",
                List.of(new OrderItemRequest(product.getId(), 1))
        );
        Long orderId = orderService.createOrder(user.getUserId(), request);

        // then
        // 실결제금액: 49000원 (50000 - 1000)
        var order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getPayAmount()).isEqualByComparingTo(BigDecimal.valueOf(49000));

        // 잔액: 0(사용) + 490(1% 적립) = 490
        long balance = pointService.getBalance(user.getUserId());
        assertThat(balance).isEqualTo(490L);

        List<PointHistory> histories = pointHistoryRepository.findByUserOrderByCreatedAtDesc(
                userRepository.findByUserId(user.getUserId()).orElseThrow()
        );
        assertThat(histories).hasSize(2);
        assertThat(histories).extracting(PointHistory::getType)
                .containsExactlyInAnyOrder(PointType.USE, PointType.EARN);

        System.out.println("실결제금액: " + order.getPayAmount() + ", 최종잔액: " + balance);
    }

    @Test
    void 포인트_잔액_부족시_예외가_발생한다() {
        // given - 유저 포인트 잔액: 0
        CreateRequest request = new CreateRequest(
                null, 5000L,
                BigDecimal.valueOf(50000),
                "12345", "서울시 강남구", "101호",
                List.of(new OrderItemRequest(product.getId(), 1))
        );

        // then
        assertThatThrownBy(() -> orderService.createOrder(user.getUserId(), request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("포인트가 부족합니다.");

        System.out.println("포인트 잔액 부족 예외 확인 완료");
    }

    @Test
    void 주문_취소시_적립된_포인트가_회수된다() {
        // given - 주문 후 500포인트 적립
        CreateRequest request = new CreateRequest(
                null, null,
                BigDecimal.valueOf(50000),
                "12345", "서울시 강남구", "101호",
                List.of(new OrderItemRequest(product.getId(), 1))
        );
        Long orderId = orderService.createOrder(user.getUserId(), request);
        assertThat(pointService.getBalance(user.getUserId())).isEqualTo(500L);

        // when - 주문 취소
        orderService.cancelOrder(user.getUserId(), orderId);

        // then - 적립 포인트 회수 → 잔액 0
        long balance = pointService.getBalance(user.getUserId());
        assertThat(balance).isEqualTo(0L);

        List<PointHistory> histories = pointHistoryRepository.findByUserOrderByCreatedAtDesc(
                userRepository.findByUserId(user.getUserId()).orElseThrow()
        );
        assertThat(histories).extracting(PointHistory::getType)
                .contains(PointType.EARN, PointType.EARN_CANCEL);

        System.out.println("취소 후 포인트 잔액: " + balance);
    }

    @Test
    void 주문_취소시_사용한_포인트가_환불된다() {
        // given - 유저에게 2000포인트 부여 후 1000포인트 사용 주문
        user = userRepository.findByUserId(user.getUserId()).orElseThrow();
        user.addPoints(2000L);
        userRepository.save(user);

        CreateRequest request = new CreateRequest(
                null, 1000L,
                BigDecimal.valueOf(50000),
                "12345", "서울시 강남구", "101호",
                List.of(new OrderItemRequest(product.getId(), 1))
        );
        Long orderId = orderService.createOrder(user.getUserId(), request);
        // 주문 후 잔액: 2000 - 1000(사용) + 490(1% 적립) = 1490
        assertThat(pointService.getBalance(user.getUserId())).isEqualTo(1490L);

        // when - 주문 취소
        orderService.cancelOrder(user.getUserId(), orderId);

        // then - 사용 포인트 환불(+1000), 적립 포인트 회수(-490) → 2010
        // 실제로는 forceDeduct이므로: 1490 + 1000 - 490 = 2000
        long balance = pointService.getBalance(user.getUserId());
        assertThat(balance).isEqualTo(2000L);

        List<PointHistory> histories = pointHistoryRepository.findByUserOrderByCreatedAtDesc(
                userRepository.findByUserId(user.getUserId()).orElseThrow()
        );
        assertThat(histories).extracting(PointHistory::getType)
                .contains(PointType.EARN, PointType.USE, PointType.EARN_CANCEL, PointType.REFUND);

        System.out.println("취소 후 포인트 잔액: " + balance + " (원래: 2000)");
    }
}
