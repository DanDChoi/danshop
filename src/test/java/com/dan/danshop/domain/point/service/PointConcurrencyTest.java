package com.dan.danshop.domain.point.service;

import com.dan.danshop.domain.order.dto.CreateRequest;
import com.dan.danshop.domain.order.dto.OrderItemRequest;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PointConcurrencyTest {

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
                .userId("concurrentuser")
                .email("concurrent@test.com")
                .password("password")
                .name("동시성테스트유저")
                .role(Role.ROLE_USER)
                .build());

        product = productRepository.save(Product.builder()
                .productName("동시성테스트상품")
                .price(BigDecimal.valueOf(10000))
                .stock(200)
                .build());
    }

    @Test
    void 동시에_10명이_포인트_사용해도_잔액이_음수가_되지_않는다() throws InterruptedException {
        // given - 유저에게 포인트 50,000 부여 (1000포인트씩 10번 사용 가능)
        user = userRepository.findByUserId(user.getUserId()).orElseThrow();
        user.addPoints(50_000L);
        userRepository.save(user);

        int threadCount = 10;
        long usePointsPerOrder = 10_000L; // 각 요청마다 10,000포인트 사용 시도

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(user.getUserId(), null, List.of())
                    );
                    CreateRequest request = new CreateRequest(
                            null, usePointsPerOrder,
                            BigDecimal.valueOf(10000),
                            "12345", "서울시 강남구", "101호",
                            List.of(new OrderItemRequest(product.getId(), 1))
                    );
                    orderService.createOrder(user.getUserId(), request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // then - 잔액이 절대 음수가 되면 안 됨
        long balance = pointService.getBalance(user.getUserId());
        assertThat(balance).isGreaterThanOrEqualTo(0L);

        // 성공한 주문 수 * usePointsPerOrder <= 초기 잔액(50,000)
        assertThat((long) successCount.get() * usePointsPerOrder).isLessThanOrEqualTo(50_000L);

        System.out.println("초기 포인트: 50,000");
        System.out.println("성공한 주문: " + successCount.get() + "건 (각 " + usePointsPerOrder + "포인트 사용)");
        System.out.println("실패한 주문: " + failCount.get() + "건");
        System.out.println("최종 포인트 잔액: " + balance);
    }
}
