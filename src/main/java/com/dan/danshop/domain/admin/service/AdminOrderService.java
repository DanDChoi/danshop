package com.dan.danshop.domain.admin.service;

import com.dan.danshop.domain.admin.dto.OrderStatusUpdateRequest;
import com.dan.danshop.domain.notification.dto.NotificationEvent;
import com.dan.danshop.domain.notification.service.NotificationService;
import com.dan.danshop.domain.order.dto.OrderResponse;
import com.dan.danshop.domain.order.entity.Order;
import com.dan.danshop.domain.order.entity.OrderStatus;
import com.dan.danshop.domain.order.repository.OrderRepository;
import com.dan.danshop.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.dan.danshop.global.exception.ErrorCode.ORDER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ORDER_NOT_FOUND));
        order.updateStatus(request.getStatus());

        String userId = order.getUser().getUserId();
        notificationService.send(userId, new NotificationEvent(
                "ORDER_STATUS_CHANGED",
                "주문 #" + orderId + " 상태가 " + request.getStatus().getDescription() + "(으)로 변경되었습니다.",
                orderId
        ));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> findAllOrders(int page, int size, OrderStatus status) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return orderRepository.findAllWithUserAndStatus(status, pageRequest).map(OrderResponse::from);
    }
}
