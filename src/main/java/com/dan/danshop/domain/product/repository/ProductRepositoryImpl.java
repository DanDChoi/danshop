package com.dan.danshop.domain.product.repository;

import com.dan.danshop.domain.product.dto.ProductSearchCondition;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.entity.QProduct;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

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
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(pageable.getSort().stream()
                        .map(order -> order.isAscending()
                                ? QProduct.product.id.asc()
                                : QProduct.product.id.desc())
                        .findFirst()
                        .orElse(QProduct.product.id.desc()))
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
}
