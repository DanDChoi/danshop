package com.dan.danshop.domain.cart.controller;

import com.dan.danshop.domain.order.repository.OrderItemRepository;
import com.dan.danshop.domain.order.repository.OrderRepository;
import com.dan.danshop.domain.point.repository.PointHistoryRepository;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.repository.ProductRepository;
import com.dan.danshop.domain.user.entity.Role;
import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.domain.user.repository.UserRepository;
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
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

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
    @Autowired private UserRepository userRepository;
    @Autowired private PointHistoryRepository pointHistoryRepository;
    @Autowired private RedisTemplate<String, Object> redisTemplate;
    @Autowired private JwtProvider jwtProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;
    private Product product;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        pointHistoryRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        product = productRepository.save(Product.builder()
                .productName("카트API테스트상품").price(BigDecimal.valueOf(7000)).stock(10).build());

        userRepository.save(User.builder().userId("cartapiuser").role(Role.ROLE_USER).build());

        redisTemplate.delete("cart:cartapiuser");
        redisTemplate.delete("cart:guest:test-guest-token");
        redisTemplate.delete("cart:guest:checkout-guest-token");
        redisTemplate.delete("cart:guest:no-orderer-token");
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

    @Test
    void 게스트가_주문자_정보와_함께_checkout하면_주문이_생성되고_장바구니가_비워진다() throws Exception {
        mockMvc.perform(post("/cart/" + product.getId())
                        .param("quantity", "2")
                        .header("X-Guest-Token", "checkout-guest-token"))
                .andExpect(status().isOk());

        String body = objectMapper.writeValueAsString(Map.of(
                "postNo", "12345",
                "baseAddr", "서울시 강남구",
                "detailAddr", "101호",
                "ordererName", "홍길동",
                "ordererEmail", "guest-checkout@test.com",
                "ordererPhone", "010-1234-5678"
        ));

        mockMvc.perform(post("/cart/checkout")
                        .header("X-Guest-Token", "checkout-guest-token")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists());

        mockMvc.perform(get("/cart")
                        .header("X-Guest-Token", "checkout-guest-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void 게스트가_주문자_정보없이_checkout하면_400을_반환한다() throws Exception {
        mockMvc.perform(post("/cart/" + product.getId())
                        .param("quantity", "1")
                        .header("X-Guest-Token", "no-orderer-token"))
                .andExpect(status().isOk());

        String body = objectMapper.writeValueAsString(Map.of(
                "postNo", "12345",
                "baseAddr", "서울시 강남구",
                "detailAddr", "101호"
        ));

        mockMvc.perform(post("/cart/checkout")
                        .header("X-Guest-Token", "no-orderer-token")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ORDERER_INFO_REQUIRED"));
    }

    @Test
    void 회원_checkout은_orderer_정보없이_기존대로_동작한다() throws Exception {
        String token = jwtProvider.generateAccessToken("cartapiuser", "ROLE_USER");

        mockMvc.perform(post("/cart/" + product.getId())
                        .param("quantity", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        String body = objectMapper.writeValueAsString(Map.of(
                "postNo", "12345",
                "baseAddr", "서울시 강남구",
                "detailAddr", "101호"
        ));

        mockMvc.perform(post("/cart/checkout")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists());
    }
}
