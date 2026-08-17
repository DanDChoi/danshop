package com.dan.danshop.domain.order.controller;

import com.dan.danshop.domain.order.dto.GuestOrderRequest;
import com.dan.danshop.domain.order.dto.OrderItemRequest;
import com.dan.danshop.domain.order.repository.OrderItemRepository;
import com.dan.danshop.domain.order.repository.OrderRepository;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class GuestOrderControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;
    private Product product;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();

        product = productRepository.save(Product.builder()
                .productName("게스트API테스트상품").price(BigDecimal.valueOf(10000)).stock(10).build());
    }

    @Test
    void 토큰없이_게스트_주문_생성시_201과_orderId를_반환한다() throws Exception {
        GuestOrderRequest request = new GuestOrderRequest(
                "홍길동", "guest@test.com", "010-1234-5678",
                BigDecimal.valueOf(10000),
                "12345", "서울시 강남구", "101호",
                List.of(new OrderItemRequest(product.getId(), 1))
        );

        mockMvc.perform(post("/guest/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists());

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updated.getStock()).isEqualTo(9);
    }

    @Test
    void 필수값_누락시_400을_반환한다() throws Exception {
        GuestOrderRequest request = new GuestOrderRequest(
                "", "not-an-email", "",
                BigDecimal.valueOf(10000),
                "12345", "서울시 강남구", "101호",
                List.of(new OrderItemRequest(product.getId(), 1))
        );

        mockMvc.perform(post("/guest/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
