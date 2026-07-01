package com.dan.danshop.domain.wishlist.service;

import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.repository.ProductRepository;
import com.dan.danshop.domain.wishlist.dto.WishlistResponse;
import com.dan.danshop.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class WishlistServiceTest {

    @Autowired private WishlistService wishlistService;
    @Autowired private ProductRepository productRepository;
    @Autowired private RedisTemplate<String, Object> redisTemplate;

    private static final String USER_ID = "wishlistuser";

    private Product productA;
    private Product productB;

    @BeforeEach
    void setUp() {
        redisTemplate.delete("wishlist:" + USER_ID);
        productRepository.deleteAll();

        productA = productRepository.save(Product.builder()
                .productName("상품A").price(BigDecimal.valueOf(10000)).stock(10).build());
        productB = productRepository.save(Product.builder()
                .productName("상품B").price(BigDecimal.valueOf(20000)).stock(10).build());
    }

    @Test
    void 상품을_위시리스트에_추가할_수_있다() {
        // when
        wishlistService.add(USER_ID, productA.getId());

        // then
        assertThat(wishlistService.isWished(USER_ID, productA.getId())).isTrue();
        System.out.println("찜 추가 확인: " + productA.getProductName());
    }

    @Test
    void 동일_상품을_중복_추가해도_한_번만_저장된다() {
        // when
        wishlistService.add(USER_ID, productA.getId());
        wishlistService.add(USER_ID, productA.getId());

        // then - Redis Set이므로 중복 불가
        List<WishlistResponse> wishlist = wishlistService.getWishlist(USER_ID);
        assertThat(wishlist).hasSize(1);
        System.out.println("중복 추가 후 위시리스트 크기: " + wishlist.size());
    }

    @Test
    void 위시리스트_목록을_조회할_수_있다() {
        // given
        wishlistService.add(USER_ID, productA.getId());
        wishlistService.add(USER_ID, productB.getId());

        // when
        List<WishlistResponse> wishlist = wishlistService.getWishlist(USER_ID);

        // then
        assertThat(wishlist).hasSize(2);
        assertThat(wishlist).extracting(WishlistResponse::getProductName)
                .containsExactlyInAnyOrder("상품A", "상품B");

        wishlist.forEach(w -> System.out.println("찜 상품: " + w.getProductName() + " / " + w.getPrice() + "원"));
    }

    @Test
    void 위시리스트에서_상품을_삭제할_수_있다() {
        // given
        wishlistService.add(USER_ID, productA.getId());
        wishlistService.add(USER_ID, productB.getId());

        // when
        wishlistService.remove(USER_ID, productA.getId());

        // then
        assertThat(wishlistService.isWished(USER_ID, productA.getId())).isFalse();
        assertThat(wishlistService.isWished(USER_ID, productB.getId())).isTrue();
        System.out.println("삭제 후 상품A 찜 여부: " + wishlistService.isWished(USER_ID, productA.getId()));
    }

    @Test
    void 찜하지_않은_상품은_isWished가_false를_반환한다() {
        // when & then
        assertThat(wishlistService.isWished(USER_ID, productA.getId())).isFalse();
        System.out.println("미찜 상품 isWished: false 확인");
    }

    @Test
    void 위시리스트_전체를_초기화할_수_있다() {
        // given
        wishlistService.add(USER_ID, productA.getId());
        wishlistService.add(USER_ID, productB.getId());

        // when
        wishlistService.clear(USER_ID);

        // then
        assertThat(wishlistService.getWishlist(USER_ID)).isEmpty();
        System.out.println("초기화 후 위시리스트 크기: 0");
    }

    @Test
    void 존재하지_않는_상품을_찜하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> wishlistService.add(USER_ID, 99999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다.");
        System.out.println("존재하지 않는 상품 찜 시도 예외 확인");
    }
}
