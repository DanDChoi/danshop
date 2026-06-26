package com.dan.danshop.domain.review.service;

import com.dan.danshop.domain.order.dto.CreateRequest;
import com.dan.danshop.domain.order.dto.OrderItemRequest;
import com.dan.danshop.domain.order.entity.OrderStatus;
import com.dan.danshop.domain.order.repository.OrderItemRepository;
import com.dan.danshop.domain.order.repository.OrderRepository;
import com.dan.danshop.domain.order.service.OrderService;
import com.dan.danshop.domain.point.repository.PointHistoryRepository;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.repository.ProductRepository;
import com.dan.danshop.domain.review.dto.CreateReviewRequest;
import com.dan.danshop.domain.review.dto.ProductReviewResponse;
import com.dan.danshop.domain.review.dto.UpdateReviewRequest;
import com.dan.danshop.domain.review.entity.Review;
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
public class ReviewServiceTest {

    @Autowired private ReviewService reviewService;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private PointHistoryRepository pointHistoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        pointHistoryRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .userId("reviewer")
                .email("review@test.com")
                .password("password")
                .name("리뷰어")
                .role(Role.ROLE_USER)
                .build());

        product = productRepository.save(Product.builder()
                .productName("리뷰테스트상품")
                .price(BigDecimal.valueOf(30000))
                .stock(100)
                .build());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getUserId(), null, List.of())
        );
    }

    private void placeOrder() {
        orderService.createOrder(new CreateRequest(
                null, null,
                BigDecimal.valueOf(30000),
                "12345", "서울시 강남구", "101호",
                List.of(new OrderItemRequest(product.getId(), 1))
        ));
    }

    @Test
    void 구매한_상품에_리뷰를_작성할_수_있다() {
        // given
        placeOrder();

        // when
        Long reviewId = reviewService.createReview(product.getId(), new CreateReviewRequest(5, "최고의 상품입니다!"));

        // then
        Review review = reviewRepository.findById(reviewId).orElseThrow();
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getContent()).isEqualTo("최고의 상품입니다!");
        assertThat(review.getUser().getUserId()).isEqualTo(user.getUserId());

        // 상품 평균 평점 업데이트 확인
        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updated.getAvgRating()).isEqualTo(5.0);

        System.out.println("리뷰 작성 완료. 평균 평점: " + updated.getAvgRating());
    }

    @Test
    void 구매하지_않은_상품에는_리뷰를_작성할_수_없다() {
        // given - 주문 없음

        // then
        assertThatThrownBy(() ->
                reviewService.createReview(product.getId(), new CreateReviewRequest(4, "좋아요"))
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("구매한 상품만 리뷰를 작성할 수 있습니다.");

        System.out.println("미구매 상품 리뷰 작성 시도 예외 확인");
    }

    @Test
    void 동일_상품에_리뷰를_중복_작성할_수_없다() {
        // given - 구매 후 첫 번째 리뷰 작성
        placeOrder();
        reviewService.createReview(product.getId(), new CreateReviewRequest(5, "좋아요"));

        // then - 두 번째 리뷰 시도
        assertThatThrownBy(() ->
                reviewService.createReview(product.getId(), new CreateReviewRequest(3, "또 써봐요"))
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 작성한 리뷰입니다.");

        System.out.println("중복 리뷰 작성 시도 예외 확인");
    }

    @Test
    void 취소된_주문은_구매_확인에서_제외된다() {
        // given - 주문 후 취소
        placeOrder();
        Long orderId = orderRepository.findAll().get(0).getId();
        orderService.cancelOrder(orderId);

        // then - 취소 주문으로는 리뷰 불가
        assertThatThrownBy(() ->
                reviewService.createReview(product.getId(), new CreateReviewRequest(4, "취소했는데 써봐요"))
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("구매한 상품만 리뷰를 작성할 수 있습니다.");

        System.out.println("취소 주문으로 리뷰 작성 시도 예외 확인");
    }

    @Test
    void 리뷰를_수정하면_평균_평점이_재계산된다() {
        // given - 리뷰 작성
        placeOrder();
        Long reviewId = reviewService.createReview(product.getId(), new CreateReviewRequest(5, "처음엔 좋았는데"));

        // when - 평점 수정
        reviewService.updateReview(reviewId, new UpdateReviewRequest(2, "재사용 후 별로네요"));

        // then
        Review review = reviewRepository.findById(reviewId).orElseThrow();
        assertThat(review.getRating()).isEqualTo(2);
        assertThat(review.getContent()).isEqualTo("재사용 후 별로네요");

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updated.getAvgRating()).isEqualTo(2.0);

        System.out.println("리뷰 수정 후 평균 평점: " + updated.getAvgRating());
    }

    @Test
    void 리뷰를_삭제하면_평균_평점이_초기화된다() {
        // given
        placeOrder();
        Long reviewId = reviewService.createReview(product.getId(), new CreateReviewRequest(4, "삭제할 리뷰"));

        // when
        reviewService.deleteReview(reviewId);

        // then
        assertThat(reviewRepository.findById(reviewId)).isEmpty();

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updated.getAvgRating()).isNull();

        System.out.println("리뷰 삭제 후 평균 평점: " + updated.getAvgRating());
    }

    @Test
    void 상품_리뷰_목록_조회시_평균_평점과_리뷰가_반환된다() {
        // given - 유저 2명이 각각 리뷰 작성
        placeOrder();
        reviewService.createReview(product.getId(), new CreateReviewRequest(4, "첫 번째 리뷰"));

        User user2 = userRepository.save(User.builder()
                .userId("reviewer2").email("r2@test.com")
                .password("pw").name("두번째유저").role(Role.ROLE_USER).build());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user2.getUserId(), null, List.of())
        );
        orderService.createOrder(new CreateRequest(
                null, null, BigDecimal.valueOf(30000),
                "12345", "서울시", "102호",
                List.of(new OrderItemRequest(product.getId(), 1))
        ));
        reviewService.createReview(product.getId(), new CreateReviewRequest(2, "두 번째 리뷰"));

        // when
        ProductReviewResponse response = reviewService.getReviews(product.getId());

        // then
        assertThat(response.getReviewCount()).isEqualTo(2);
        assertThat(response.getAvgRating()).isEqualTo(3.0); // (4+2)/2

        System.out.println("리뷰 수: " + response.getReviewCount() + ", 평균 평점: " + response.getAvgRating());
    }

    @Test
    void 타인의_리뷰는_수정할_수_없다() {
        // given - user1 리뷰 작성
        placeOrder();
        Long reviewId = reviewService.createReview(product.getId(), new CreateReviewRequest(5, "user1 리뷰"));

        // user2로 로그인
        userRepository.save(User.builder()
                .userId("other").email("other@test.com")
                .password("pw").name("타인").role(Role.ROLE_USER).build());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("other", null, List.of())
        );

        // then
        assertThatThrownBy(() ->
                reviewService.updateReview(reviewId, new UpdateReviewRequest(1, "내가 바꿨다"))
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("본인의 리뷰만 수정/삭제할 수 있습니다.");

        System.out.println("타인 리뷰 수정 시도 예외 확인");
    }
}
