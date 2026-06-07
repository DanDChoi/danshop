package com.dan.danshop.domain.user.service;

import com.dan.danshop.domain.user.dto.LoginRequest;
import com.dan.danshop.domain.user.dto.SignupRequest;
import com.dan.danshop.domain.user.dto.TokenResponse;
import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.domain.user.repository.UserRepository;
import com.dan.danshop.global.config.JwtProvider;
import com.dan.danshop.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.dan.danshop.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    public void userSignup(SignupRequest signupRequest) {
        if (userRepository.findByUserId(signupRequest.getUserId()).isPresent()) {
            throw new BusinessException(DUPLICATED_USER_ID);
        }
        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());
        User newUser = User.from(signupRequest, encodedPassword);
        userRepository.save(newUser);
    }

    public TokenResponse login(LoginRequest loginRequest) {
        User existsUser = userRepository.findByUserId(loginRequest.getUserId())
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        if (!passwordEncoder.matches(loginRequest.getPassword(), existsUser.getPassword())) {
            throw new BusinessException(PASSWORD_NOT_MATCH);
        }

        String accessToken = jwtProvider.generateAccessToken(existsUser.getUserId(), existsUser.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(existsUser.getUserId());

        // Refresh Token Redis 저장 (7일 TTL)
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + existsUser.getUserId(),
                refreshToken,
                refreshExpiration,
                TimeUnit.MILLISECONDS
        );

        return new TokenResponse(accessToken, refreshToken);
    }
}
