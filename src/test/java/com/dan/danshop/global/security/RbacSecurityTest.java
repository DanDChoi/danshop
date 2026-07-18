package com.dan.danshop.global.security;

import com.dan.danshop.global.config.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class RbacSecurityTest {

    @Autowired private WebApplicationContext context;
    @Autowired private JwtProvider jwtProvider;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // ───────────────────────────────────────────
    // 401: 토큰 없이 접근
    // ───────────────────────────────────────────

    @Test
    void 토큰없이_admin_엔드포인트_접근시_401() throws Exception {
        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 토큰없이_일반_엔드포인트_접근시_401() throws Exception {
        mockMvc.perform(get("/coupons"))
                .andExpect(status().isUnauthorized());
    }

    // ───────────────────────────────────────────
    // 403: ROLE_USER로 ADMIN 엔드포인트 접근
    // ───────────────────────────────────────────

    @Test
    void ROLE_USER로_admin_엔드포인트_접근시_403() throws Exception {
        String userToken = jwtProvider.generateAccessToken("testuser", "ROLE_USER");

        mockMvc.perform(get("/admin/orders")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ───────────────────────────────────────────
    // 200: ROLE_ADMIN으로 ADMIN 엔드포인트 정상 접근
    // ───────────────────────────────────────────

    @Test
    void ROLE_ADMIN으로_admin_엔드포인트_접근시_200() throws Exception {
        String adminToken = jwtProvider.generateAccessToken("admin", "ROLE_ADMIN");

        mockMvc.perform(get("/admin/orders")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
