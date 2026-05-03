package com.dan.danshop.domain.order.service;

import com.dan.danshop.domain.order.dto.CreateRequest;
import com.dan.danshop.domain.order.dto.OrderItemRequest;
import com.dan.danshop.domain.order.dto.OrderResponse;
import com.dan.danshop.domain.order.entity.Order;
import com.dan.danshop.domain.order.entity.OrderItem;
import com.dan.danshop.domain.order.repository.OrderItemRepository;
import com.dan.danshop.domain.order.repository.OrderRepository;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.repository.ProductRepository;
import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public Long createOrder(CreateRequest createRequest) {

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

        return newOrder.getId();
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        //현재 로그인유저 조회
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User curruntUser = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("유저 없음"));

        //취소요청 주문 조회
        Order cancelOrder = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("없는 주문"));

        //로그인 유저의 주문인지 확인
        if (!cancelOrder.getUser().getId().equals(curruntUser.getId())) {
            throw new RuntimeException("본인의 주문만 취소 가능합니다");
        }

        //취소
        cancelOrder.cancel();

        //재고복구
        List<OrderItem> orderItems = orderItemRepository.findByOrder(cancelOrder);
        for (OrderItem orderItem : orderItems) {
            Product orderdProduct = productRepository.findByIdWithLock(orderItem.getProduct().getId()).orElseThrow(() -> new RuntimeException("상품 없음"));
            orderdProduct.increaseStock(orderItem.getQuantity());
        }
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> findOrderList(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        return orderRepository.findAllWithUser(pageRequest)
                .map(OrderResponse::from);

    }
}
