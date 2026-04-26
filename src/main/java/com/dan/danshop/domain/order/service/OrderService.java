package com.dan.danshop.domain.order.service;

import com.dan.danshop.domain.order.dto.CreateRequest;
import com.dan.danshop.domain.order.dto.OrderItemRequest;
import com.dan.danshop.domain.order.entity.Order;
import com.dan.danshop.domain.order.entity.OrderItem;
import com.dan.danshop.domain.order.repository.OrderRepository;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.repository.ProductRepository;
import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public void createOrder(CreateRequest createRequest) {

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User curruntUser = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("유저 없음"));

        Order newOrder = Order.from(createRequest, curruntUser);

        //TODO
        List<OrderItemRequest> itemRequests = new ArrayList<>();
        for (int i = 0; i < createRequest.getItems().size(); i++) {
            Product product = productRepository.findByProductNo(createRequest.getItems().get(i).getProductId()).orElseThrow(() -> new RuntimeException("상품 없음"));

        }

        orderRepository.save(newOrder);
    }
}
