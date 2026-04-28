package com.dan.danshop.domain.order.service;

import com.dan.danshop.domain.order.dto.CreateRequest;
import com.dan.danshop.domain.order.dto.OrderItemRequest;
import com.dan.danshop.domain.order.entity.Order;
import com.dan.danshop.domain.order.entity.OrderItem;
import com.dan.danshop.domain.order.repository.OrderItemRepository;
import com.dan.danshop.domain.order.repository.OrderRepository;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.repository.ProductRepository;
import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void createOrder(CreateRequest createRequest) {

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User curruntUser = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("유저 없음"));

        Order newOrder = Order.from(createRequest, curruntUser);


        List<OrderItem> itemRequests = new ArrayList<>();
        for (OrderItemRequest itemRequest : createRequest.getItems()) {
            //product 조회
//            Product product = productRepository.findById(itemRequest.getProductId()).orElseThrow(() -> new RuntimeException("상품 없음"));
            //product 조회 (비관적 락)
            Product product = productRepository.findByIdWithLock(itemRequest.getProductId()).orElseThrow(() -> new RuntimeException("상품 없음"));
            //재고 차감
            product.decreaseStock(itemRequest.getQuantity());
            OrderItem orderItem = OrderItem.from(newOrder, product, itemRequest.getQuantity());
            itemRequests.add(orderItem);
        }

        orderRepository.save(newOrder);
        orderItemRepository.saveAll(itemRequests);
    }

    public void cancelOrder(Long orderId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User curruntUser = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("유저 없음"));

        Order cancelOrder = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("없는 주문"));

        if (cancelOrder.getUser().getId() != curruntUser.getId()) {
            throw new RuntimeException("본인의 주문만 취소 가능합니다");
        }
        //TODO
//        orderRepository.cancelOrder();

    }
}
