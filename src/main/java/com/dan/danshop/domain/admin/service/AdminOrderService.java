package com.dan.danshop.domain.admin.service;

import com.dan.danshop.domain.admin.dto.OrderStatusUpdateRequest;
import com.dan.danshop.domain.order.entity.Order;
import com.dan.danshop.domain.order.repository.OrderRepository;
import com.dan.danshop.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.dan.danshop.global.exception.ErrorCode.ORDER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ORDER_NOT_FOUND));
        order.updateStatus(request.getStatus());
    }
}
