package com.dan.danshop.domain.product.repository;

import com.dan.danshop.domain.product.dto.ProductSearchCondition;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.entity.QProduct;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // ── Offset 기반 페이지네이션 (동적 정렬 지원) ──
    @Override
    public Page<Product> searchProducts(ProductSearchCondition condition, Pageable pageable) {
        QProduct product = QProduct.product;

        List<Product> content = queryFactory
                .selectFrom(product)
                .where(
                        nameContains(condition.getKeyword()),
                        categoryEq(condition.getCategory()),
                        priceGoe(condition.getMinPrice()),
                        priceLoe(condition.getMaxPrice())
                )
                .orderBy(getOrderSpecifier(condition.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(
                        nameContains(condition.getKeyword()),
                        categoryEq(condition.getCategory()),
                        priceGoe(condition.getMinPrice()),
                        priceLoe(condition.getMaxPrice())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    // ── No-offset 커서 기반 페이지네이션 ──
    @Override
    public List<Product> searchProductsNoOffset(ProductSearchCondition condition, Long lastId, int size) {
        // size + 1개 조회해서 hasNext 판별은 서비스 레이어에서 처리
        return queryFactory
                .selectFrom(QProduct.product)
                .where(
                        ltLastId(lastId),
                        nameContains(condition.getKeyword()),
                        categoryEq(condition.getCategory()),
                        priceGoe(condition.getMinPrice()),
                        priceLoe(condition.getMaxPrice())
                )
                .orderBy(QProduct.product.id.desc())
                .limit(size + 1L)
                .fetch();
    }

    // ── BooleanExpression 조건 메서드 (null 반환 시 WHERE 절 자동 제외) ──

    private BooleanExpression nameContains(String keyword) {
        return (keyword != null && !keyword.isBlank())
                ? QProduct.product.productName.containsIgnoreCase(keyword)
                : null;
    }

    private BooleanExpression categoryEq(String category) {
        return (category != null && !category.isBlank())
                ? QProduct.product.category.eq(category)
                : null;
    }

    private BooleanExpression priceGoe(BigDecimal minPrice) {
        return minPrice != null ? QProduct.product.price.goe(minPrice) : null;
    }

    private BooleanExpression priceLoe(BigDecimal maxPrice) {
        return maxPrice != null ? QProduct.product.price.loe(maxPrice) : null;
    }

    private BooleanExpression ltLastId(Long lastId) {
        return lastId != null ? QProduct.product.id.lt(lastId) : null;
    }

    // ── 동적 정렬 ──
    private OrderSpecifier<?> getOrderSpecifier(String sort) {
        if (sort == null) return QProduct.product.id.desc();
        return switch (sort) {
            case "price_asc"  -> QProduct.product.price.asc();
            case "price_desc" -> QProduct.product.price.desc();
            default           -> QProduct.product.id.desc(); // latest
        };
    }
}
