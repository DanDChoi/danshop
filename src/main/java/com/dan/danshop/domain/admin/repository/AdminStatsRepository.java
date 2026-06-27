package com.dan.danshop.domain.admin.repository;

import com.dan.danshop.domain.admin.dto.OrderStatusStatResponse;
import com.dan.danshop.domain.admin.dto.ProductSalesResponse;
import com.dan.danshop.domain.admin.dto.SalesStatResponse;
import com.dan.danshop.domain.order.entity.OrderStatus;
import com.dan.danshop.domain.order.entity.QOrder;
import com.dan.danshop.domain.order.entity.QOrderItem;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AdminStatsRepository {

    private final JPAQueryFactory queryFactory;

    private static final QOrder order = QOrder.order;
    private static final QOrderItem orderItem = QOrderItem.orderItem;

    public SalesStatResponse getSalesStat(LocalDateTime from, LocalDateTime to) {
        Tuple result = queryFactory
                .select(order.payAmount.sum(), order.count())
                .from(order)
                .where(
                        order.status.eq(OrderStatus.PAID),
                        goeFrom(from),
                        loeTo(to)
                )
                .fetchOne();

        BigDecimal totalSales = result != null && result.get(order.payAmount.sum()) != null
                ? result.get(order.payAmount.sum()) : BigDecimal.ZERO;
        Long orderCount = result != null && result.get(order.count()) != null
                ? result.get(order.count()) : 0L;

        return new SalesStatResponse(totalSales, orderCount, from, to);
    }

    public List<ProductSalesResponse> getTopProductsBySales(int limit) {
        List<Tuple> results = queryFactory
                .select(
                        orderItem.product.id,
                        orderItem.product.productName,
                        orderItem.quantity.sum(),
                        orderItem.price.multiply(orderItem.quantity).sum()
                )
                .from(orderItem)
                .join(orderItem.order, order)
                .where(order.status.ne(OrderStatus.CANCELLED))
                .groupBy(orderItem.product.id, orderItem.product.productName)
                .orderBy(orderItem.quantity.sum().desc())
                .limit(limit)
                .fetch();

        return results.stream().map(t -> new ProductSalesResponse(
                t.get(orderItem.product.id),
                t.get(orderItem.product.productName),
                t.get(orderItem.quantity.sum()),
                t.get(orderItem.price.multiply(orderItem.quantity).sum())
        )).toList();
    }

    public List<OrderStatusStatResponse> getOrderStatusStat() {
        return queryFactory
                .select(order.status, order.count())
                .from(order)
                .groupBy(order.status)
                .fetch()
                .stream()
                .map(t -> new OrderStatusStatResponse(
                        t.get(order.status),
                        t.get(order.count())
                ))
                .toList();
    }

    private BooleanExpression goeFrom(LocalDateTime from) {
        return from != null ? order.createdAt.goe(from) : null;
    }

    private BooleanExpression loeTo(LocalDateTime to) {
        return to != null ? order.createdAt.loe(to) : null;
    }
}
