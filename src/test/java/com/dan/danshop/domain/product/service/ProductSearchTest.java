package com.dan.danshop.domain.product.service;

import com.dan.danshop.domain.product.dto.ProductCursorResponse;
import com.dan.danshop.domain.product.dto.ProductResponse;
import com.dan.danshop.domain.product.dto.ProductSearchCondition;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ProductSearchTest {

    @Autowired private ProductService productService;
    @Autowired private ProductRepository productRepository;
    @Autowired private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // 캐시 초기화 (테스트 간 캐시 오염 방지)
        cacheManager.getCache("products").clear();
        productRepository.deleteAll();

        productRepository.saveAll(List.of(
                Product.builder().productName("나이키 에어맥스").category("신발").price(BigDecimal.valueOf(120000)).stock(10).build(),
                Product.builder().productName("나이키 반팔티").category("의류").price(BigDecimal.valueOf(35000)).stock(20).build(),
                Product.builder().productName("아디다스 운동화").category("신발").price(BigDecimal.valueOf(90000)).stock(5).build(),
                Product.builder().productName("아디다스 트레이닝복").category("의류").price(BigDecimal.valueOf(75000)).stock(15).build(),
                Product.builder().productName("뉴발란스 스니커즈").category("신발").price(BigDecimal.valueOf(110000)).stock(8).build()
        ));
    }

    // ───────────────────────────────────────────
    // 5단계: QueryDSL 검색 조건 / 정렬 / No-offset 테스트
    // ───────────────────────────────────────────

    @Test
    void 키워드로_상품을_검색할_수_있다() {
        // given
        ProductSearchCondition condition = new ProductSearchCondition("나이키", null, null, null, null);

        // when
        Page<ProductResponse> result = productService.findProductList(0, 10, condition);

        // then - 나이키 에어맥스, 나이키 반팔티
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(p -> p.getProductName().contains("나이키"));

        System.out.println("키워드 검색 결과: " + result.getContent().stream().map(ProductResponse::getProductName).toList());
    }

    @Test
    void 카테고리로_상품을_필터링할_수_있다() {
        // given
        ProductSearchCondition condition = new ProductSearchCondition(null, "신발", null, null, null);

        // when
        Page<ProductResponse> result = productService.findProductList(0, 10, condition);

        // then - 나이키 에어맥스, 아디다스 운동화, 뉴발란스 스니커즈
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).allMatch(p -> p.getCategory().equals("신발"));

        System.out.println("신발 카테고리 필터 결과: " + result.getTotalElements() + "개");
    }

    @Test
    void 가격_범위로_상품을_필터링할_수_있다() {
        // given - 50000 ~ 100000원
        ProductSearchCondition condition = new ProductSearchCondition(null, null, BigDecimal.valueOf(50000), BigDecimal.valueOf(100000), null);

        // when
        Page<ProductResponse> result = productService.findProductList(0, 10, condition);

        // then - 아디다스 운동화(90000), 아디다스 트레이닝복(75000)
        assertThat(result.getContent()).hasSize(2);
        result.getContent().forEach(p ->
                assertThat(p.getPrice()).isBetween(BigDecimal.valueOf(50000), BigDecimal.valueOf(100000)));

        System.out.println("가격 범위 결과: " + result.getContent().stream()
                .map(p -> p.getProductName() + "(" + p.getPrice() + "원)").toList());
    }

    @Test
    void 복합_조건으로_상품을_검색할_수_있다() {
        // given - 키워드 '나이키' + 카테고리 '신발'
        ProductSearchCondition condition = new ProductSearchCondition("나이키", "신발", null, null, null);

        // when
        Page<ProductResponse> result = productService.findProductList(0, 10, condition);

        // then - 나이키 에어맥스 1개만
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProductName()).isEqualTo("나이키 에어맥스");

        System.out.println("복합 조건 결과: " + result.getContent().get(0).getProductName());
    }

    @Test
    void 가격_오름차순으로_정렬할_수_있다() {
        // given
        ProductSearchCondition condition = new ProductSearchCondition(null, null, null, null, "price_asc");

        // when
        Page<ProductResponse> result = productService.findProductList(0, 10, condition);

        // then - 첫 번째가 가장 저렴 (35000원 나이키 반팔티)
        List<BigDecimal> prices = result.getContent().stream().map(ProductResponse::getPrice).toList();
        assertThat(prices).isSortedAccordingTo(Comparator.naturalOrder());

        System.out.println("가격 오름차순: " + prices);
    }

    @Test
    void 가격_내림차순으로_정렬할_수_있다() {
        // given
        ProductSearchCondition condition = new ProductSearchCondition(null, null, null, null, "price_desc");

        // when
        Page<ProductResponse> result = productService.findProductList(0, 10, condition);

        // then - 첫 번째가 가장 비쌈 (120000원 나이키 에어맥스)
        List<BigDecimal> prices = result.getContent().stream().map(ProductResponse::getPrice).toList();
        assertThat(prices).isSortedAccordingTo(Comparator.reverseOrder());

        System.out.println("가격 내림차순: " + prices);
    }

    @Test
    void NoOffset_첫_페이지를_조회할_수_있다() {
        // given
        ProductSearchCondition condition = new ProductSearchCondition(null, null, null, null, null);

        // when - lastId 없이 첫 페이지 (size=3)
        ProductCursorResponse response = productService.findProductListNoOffset(condition, null, 3);

        // then - 3개 반환, hasNext=true
        assertThat(response.getProducts()).hasSize(3);
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.getLastId()).isNotNull();

        System.out.println("첫 페이지: " + response.getProducts().size() + "개, hasNext=" + response.isHasNext() + ", lastId=" + response.getLastId());
    }

    @Test
    void NoOffset_다음_페이지를_연속으로_조회할_수_있다() {
        // given - 첫 페이지에서 lastId 획득
        ProductSearchCondition condition = new ProductSearchCondition(null, null, null, null, null);
        ProductCursorResponse firstPage = productService.findProductListNoOffset(condition, null, 3);
        Long lastId = firstPage.getLastId();

        // when - lastId 기반 두 번째 페이지 조회
        ProductCursorResponse secondPage = productService.findProductListNoOffset(condition, lastId, 3);

        // then - 나머지 2개, hasNext=false
        assertThat(secondPage.getProducts()).hasSize(2);
        assertThat(secondPage.isHasNext()).isFalse();

        System.out.println("두 번째 페이지: " + secondPage.getProducts().size() + "개, hasNext=" + secondPage.isHasNext());
    }
}
