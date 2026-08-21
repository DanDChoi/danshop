package com.dan.danshop.domain.cart.controller;

import com.dan.danshop.domain.order.repository.OrderItemRepository;
import com.dan.danshop.domain.order.repository.OrderRepository;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.repository.ProductRepository;
import com.dan.danshop.global.config.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class CartControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private RedisTemplate<String, Object> redisTemplate;
    @Autowired private JwtProvider jwtProvider;

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
                .productName("카트API테스트상품").price(BigDecimal.valueOf(7000)).stock(10).build());

        redisTemplate.delete("cart:cartapiuser");
        redisTemplate.delete("cart:guest:test-guest-token");
    }

    @Test
    void 게스트_토큰으로_토큰없이_장바구니에_담고_조회할_수_있다() throws Exception {
        mockMvc.perform(post("/cart/" + product.getId())
                        .param("quantity", "2")
                        .header("X-Guest-Token", "test-guest-token"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/cart")
                        .header("X-Guest-Token", "test-guest-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void 게스트_토큰도_인증도_없으면_400을_반환한다() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GUEST_TOKEN_REQUIRED"));
    }

    @Test
    void 회원은_토큰만으로_장바구니에_담고_조회할_수_있다() throws Exception {
        String token = jwtProvider.generateAccessToken("cartapiuser", "ROLE_USER");

        mockMvc.perform(post("/cart/" + product.getId())
                        .param("quantity", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(1));
    }
}
