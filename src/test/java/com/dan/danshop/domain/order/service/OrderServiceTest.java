package com.dan.danshop.domain.order.service;

import com.dan.danshop.DataSourceProxyConfig;
import com.dan.danshop.domain.order.dto.CreateRequest;
import com.dan.danshop.domain.order.dto.OrderItemRequest;
import com.dan.danshop.domain.order.dto.UpdateAddressRequest;
import com.dan.danshop.domain.order.entity.Order;
import com.dan.danshop.domain.order.entity.OrderStatus;
import com.dan.danshop.domain.coupon.repository.UserCouponRepository;
import com.dan.danshop.global.exception.BusinessException;
import com.dan.danshop.domain.order.repository.OrderItemRepository;
import com.dan.danshop.domain.order.repository.OrderRepository;
import com.dan.danshop.domain.point.repository.PointHistoryRepository;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.repository.ProductRepository;
import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.domain.user.repository.UserRepository;
import net.ttddyy.dsproxy.QueryCount;
import net.ttddyy.dsproxy.QueryCountHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(DataSourceProxyConfig.class)
public class OrderServiceTest {

    @Autowired private OrderService orderService;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private PointHistoryRepository pointHistoryRepository;
    @Autowired private UserCouponRepository userCouponRepository;

    @BeforeEach
    void setUp() {
        pointHistoryRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        userCouponRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }


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
                        null,
                        null,
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
                null,
                null,
                BigDecimal.valueOf(10000),
                "12345",
                "서울시 강남구",
                "101호",
                List.of(new OrderItemRequest(product.getId(), 1))
        );
        Long orderId = orderService.createOrder(request);

        int stock = productRepository.findById(product.getId()).get().getStock();

        //주문취소
        orderService.cancelOrder("testuser", orderId);

        // 취소 후 결과 확인
        Order cancelledOrder = orderRepository.findById(orderId).get();
        Product updatedProduct = productRepository.findById(product.getId()).get();

        System.out.println("주문 후 재고: "+stock);
        System.out.println("주문 상태: " + cancelledOrder.getStatus());
        System.out.println("복구된 재고: " + updatedProduct.getStock());
    }

    @Test
    void PENDING_상태에서_배송지를_변경할_수_있다() {
        Product product = productRepository.save(Product.builder()
                .productName("배송지테스트상품").price(BigDecimal.valueOf(10000)).stock(10).build());
        User user = userRepository.save(User.builder().userId("addruser").build());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("addruser", null, List.of())
        );
        Long orderId = orderService.createOrder(new CreateRequest(
                null, null, BigDecimal.valueOf(10000),
                "12345", "서울시 강남구", "101호",
                List.of(new OrderItemRequest(product.getId(), 1))
        ));

        UpdateAddressRequest request = new UpdateAddressRequest();
        ReflectionTestUtils.setField(request, "postNo", "99999");
        ReflectionTestUtils.setField(request, "baseAddr", "부산시 해운대구");
        ReflectionTestUtils.setField(request, "detailAddr", "202호");

        orderService.updateAddress("addruser", orderId, request);

        Order updated = orderRepository.findById(orderId).orElseThrow();
        assertThat(updated.getPostNo()).isEqualTo("99999");
        assertThat(updated.getBaseAddr()).isEqualTo("부산시 해운대구");
        assertThat(updated.getDetailAddr()).isEqualTo("202호");

        System.out.println("배송지 변경 완료: " + updated.getBaseAddr() + " " + updated.getDetailAddr());
    }

    @Test
    void PAID_상태에서는_배송지를_변경할_수_없다() {
        Product product = productRepository.save(Product.builder()
                .productName("배송지테스트상품2").price(BigDecimal.valueOf(10000)).stock(10).build());
        userRepository.save(User.builder().userId("addruser2").build());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("addruser2", null, List.of())
        );
        Long orderId = orderService.createOrder(new CreateRequest(
                null, null, BigDecimal.valueOf(10000),
                "12345", "서울시 강남구", "101호",
                List.of(new OrderItemRequest(product.getId(), 1))
        ));

        // PENDING → PAID 상태로 변경
        Order order = orderRepository.findById(orderId).orElseThrow();
        ReflectionTestUtils.setField(order, "status", OrderStatus.PAID);
        orderRepository.save(order);

        UpdateAddressRequest request = new UpdateAddressRequest();
        ReflectionTestUtils.setField(request, "postNo", "99999");
        ReflectionTestUtils.setField(request, "baseAddr", "부산시 해운대구");
        ReflectionTestUtils.setField(request, "detailAddr", "202호");

        assertThatThrownBy(() -> orderService.updateAddress("addruser2", orderId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("배송 준비 전 주문만 배송지를 변경할 수 있습니다.");

        System.out.println("PAID 상태 배송지 변경 시도 예외 확인");
    }

    @Test
    void N플러스1_재현_테스트() {
        //테스트용 상품 준비
        Product product = Product.builder()
                .productName("테스트상품")
                .price(BigDecimal.valueOf(10000))
                .stock(10)
                .build();
        productRepository.save(product);

        //유저3 주문3
        for (int i = 0; i < 3; i++) {
            User user = User.builder()
                    .userId("testuser" + i)
                    .build();
            userRepository.save(user);

            //주문 생성 호출
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("testuser"+ i, null, List.of())
            );
            CreateRequest request = new CreateRequest(
                    null,
                    null,
                    BigDecimal.valueOf(10000),
                    "12345",
                    "서울시 강남구",
                    "101호",
                    List.of(new OrderItemRequest(product.getId(), 1))
            );
            Long orderId = orderService.createOrder(request);
        }

        QueryCountHolder.clear();

        orderService.findOrderList("testuser2", 0, 10);

        QueryCount count = QueryCountHolder.getGrandTotal();
        System.out.println("SELECT: " + count.getSelect());
        System.out.println("INSERT: " + count.getInsert());
        System.out.println("UPDATE: " + count.getUpdate());
        System.out.println("전체: " + count.getTotal());
    }
}
