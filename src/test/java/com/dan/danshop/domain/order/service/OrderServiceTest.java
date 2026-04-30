package com.dan.danshop.domain.order.service;

import com.dan.danshop.domain.order.dto.CreateRequest;
import com.dan.danshop.domain.order.dto.OrderItemRequest;
import com.dan.danshop.domain.order.entity.Order;
import com.dan.danshop.domain.order.repository.OrderRepository;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.repository.ProductRepository;
import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;


    @Test
    void 동시_주문_테스트() throws InterruptedException {
        //실패 요청수 카운트용
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        //테스트용 상품 준비
        Product product = Product.builder()
                .productName("테스트상품")
                .price(BigDecimal.valueOf(10000))
                .stock(10)
                .build();
        productRepository.save(product);

        //유저
        User user = User.builder()
                .userId("testuser")
                .build();
        userRepository.save(user);

        //스레드 동시 실행
        ExecutorService executor = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                //주문 생성 호출
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken("testuser", null, List.of())
                );
                CreateRequest request = new CreateRequest(
                        BigDecimal.valueOf(10000),
                        "12345",
                        "서울시 강남구",
                        "101호",
                        List.of(new OrderItemRequest(product.getId(), 1))
                );
                try {
                    orderService.createOrder(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            });

        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        //결과 확인
        Product result = productRepository.findById(product.getId()).get();
        System.out.println("남은 재고: "+result.getStock());
        System.out.println("생성된 주문 수: "+orderRepository.count());
        System.out.println("성공: " + successCount.get());
        System.out.println("실패: " + failCount.get());
    }

    @Test
    void 주문_취소_테스트() {
        //테스트용 상품 준비
        Product product = Product.builder()
                .productName("테스트상품")
                .price(BigDecimal.valueOf(10000))
                .stock(10)
                .build();
        productRepository.save(product);

        //유저
        User user = User.builder()
                .userId("testuser")
                .build();
        userRepository.save(user);

        //주문 생성 호출
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", null, List.of())
        );
        CreateRequest request = new CreateRequest(
                BigDecimal.valueOf(10000),
                "12345",
                "서울시 강남구",
                "101호",
                List.of(new OrderItemRequest(product.getId(), 1))
        );
        Long orderId = orderService.createOrder(request);

        int stock = productRepository.findById(product.getId()).get().getStock();

        //주문취소
        orderService.cancelOrder(orderId);

        // 취소 후 결과 확인
        Order cancelledOrder = orderRepository.findById(orderId).get();
        Product updatedProduct = productRepository.findById(product.getId()).get();

        System.out.println("주문 후 재고: "+stock);
        System.out.println("주문 상태: " + cancelledOrder.getStatus());
        System.out.println("복구된 재고: " + updatedProduct.getStock());
    }
}
