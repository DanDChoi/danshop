package com.dan.danshop.domain.order.service;

import com.dan.danshop.domain.coupon.entity.Coupon;
import com.dan.danshop.domain.coupon.entity.DiscountType;
import com.dan.danshop.domain.coupon.entity.UserCoupon;
import com.dan.danshop.domain.coupon.repository.UserCouponRepository;
import com.dan.danshop.domain.order.dto.CreateRequest;
import com.dan.danshop.domain.order.dto.OrderDetailResponse;
import com.dan.danshop.domain.order.dto.OrderItemRequest;
import com.dan.danshop.domain.order.dto.OrderResponse;
import com.dan.danshop.domain.order.entity.Order;
import com.dan.danshop.domain.order.entity.OrderItem;
import com.dan.danshop.domain.order.repository.OrderItemRepository;
import com.dan.danshop.domain.order.repository.OrderRepository;
import com.dan.danshop.domain.notification.dto.NotificationEvent;
import com.dan.danshop.domain.notification.service.NotificationService;
import com.dan.danshop.domain.point.service.PointService;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.repository.ProductRepository;
import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.domain.user.repository.UserRepository;
import com.dan.danshop.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.dan.danshop.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final UserCouponRepository userCouponRepository;
    private final PointService pointService;
    private final NotificationService notificationService;

    @Transactional
    public Long createOrder(CreateRequest createRequest) {

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        // 포인트 사용 시 동시 차감 방지를 위해 비관적 락으로 유저 조회
        User curruntUser = (createRequest.getUsePoints() != null && createRequest.getUsePoints() > 0)
                ? userRepository.findByUserIdWithLock(userId).orElseThrow(() -> new BusinessException(USER_NOT_FOUND))
                : userRepository.findByUserId(userId).orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        // 쿠폰 적용
        BigDecimal payAmount = createRequest.getPayAmount();
        if (createRequest.getCouponId() != null) {
            UserCoupon userCoupon = userCouponRepository
                    .findByUserIdAndCouponId(curruntUser.getId(), createRequest.getCouponId())
                    .orElseThrow(() -> new BusinessException(COUPON_NOT_OWNED));

            if (userCoupon.isUsed()) throw new BusinessException(COUPON_ALREADY_USED);

            Coupon coupon = userCoupon.getCoupon();
            if (coupon.getExpiresAt().isBefore(LocalDateTime.now())) throw new BusinessException(COUPON_EXPIRED);
            if (payAmount.compareTo(coupon.getMinOrderAmount()) < 0) throw new BusinessException(MIN_ORDER_AMOUNT_NOT_MET);

            if (coupon.getDiscountType() == DiscountType.AMOUNT) {
                payAmount = payAmount.subtract(coupon.getDiscountValue());
            } else {
                BigDecimal discountRate = coupon.getDiscountValue().divide(BigDecimal.valueOf(100));
                payAmount = payAmount.multiply(BigDecimal.ONE.subtract(discountRate)).setScale(0, RoundingMode.DOWN);
            }

            userCoupon.use();
        }

        // 포인트 사용 (잔액 차감, payAmount 감소)
        payAmount = pointService.applyPoints(curruntUser, createRequest.getUsePoints(), payAmount);

        Order newOrder = Order.from(createRequest, curruntUser, payAmount);

        List<OrderItem> itemRequests = new ArrayList<>();
        for (OrderItemRequest itemRequest : createRequest.getItems()) {
            Product product = productRepository.findByIdWithLock(itemRequest.getProductId()).orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
            product.decreaseStock(itemRequest.getQuantity());
            OrderItem orderItem = OrderItem.from(newOrder, product, itemRequest.getQuantity());
            itemRequests.add(orderItem);
        }

        orderRepository.save(newOrder);
        orderItemRepository.saveAll(itemRequests);

        // 포인트 이력 저장 (사용 / 적립)
        if (createRequest.getUsePoints() != null && createRequest.getUsePoints() > 0) {
            pointService.recordUse(curruntUser, newOrder.getId(), createRequest.getUsePoints());
        }
        pointService.earnPoints(curruntUser, newOrder.getId(), payAmount);

        notificationService.send(userId, new NotificationEvent(
                "ORDER_CREATED",
                "주문 #" + newOrder.getId() + "이 접수되었습니다.",
                newOrder.getId()
        ));

        return newOrder.getId();
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        //현재 로그인유저 조회
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User curruntUser = userRepository.findByUserId(userId).orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        //취소요청 주문 조회
        Order cancelOrder = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(ORDER_NOT_FOUND));

        //로그인 유저의 주문인지 확인
        if (!cancelOrder.getUser().getId().equals(curruntUser.getId())) {
            throw new BusinessException(NOT_ORDERED_USER);
        }

        //취소
        cancelOrder.cancel();

        //재고복구
        List<OrderItem> orderItems = orderItemRepository.findByOrder(cancelOrder);
        for (OrderItem orderItem : orderItems) {
            Product orderdProduct = productRepository.findByIdWithLock(orderItem.getProduct().getId()).orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
            orderdProduct.increaseStock(orderItem.getQuantity());
        }

        //포인트 취소
        pointService.cancelOrderPoints(curruntUser, orderId);

        notificationService.send(userId, new NotificationEvent(
                "ORDER_CANCELLED",
                "주문 #" + orderId + "이 취소되었습니다.",
                orderId
        ));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> findOrderList(int page, int size) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        PageRequest pageRequest = PageRequest.of(page, size);
        return orderRepository.findByUserIdString(userId, pageRequest).map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse findOrder(Long orderId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException(NOT_ORDERED_USER);
        }

        List<OrderItem> items = orderItemRepository.findByOrder(order);
        return OrderDetailResponse.from(order, items);
    }
}
