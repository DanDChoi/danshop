package com.dan.danshop.domain.user.service;

import com.dan.danshop.domain.user.dto.LoginRequest;
import com.dan.danshop.domain.user.dto.RefreshRequest;
import com.dan.danshop.domain.user.dto.SignupRequest;
import com.dan.danshop.domain.user.dto.TokenResponse;
import com.dan.danshop.domain.user.dto.UserProfileResponse;
import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.domain.user.repository.UserRepository;
import com.dan.danshop.global.config.JwtProvider;
import com.dan.danshop.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public String refresh(RefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();

        // 1. JWT 서명 및 만료 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException(INVALID_REFRESH_TOKEN);
        }

        // 2. 토큰에서 userId 추출
        String userId = jwtProvider.getUserId(refreshToken);

        // 3. Redis에 저장된 Refresh Token과 일치하는지 검증
        String stored = (String) redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        if (stored == null) {
            throw new BusinessException(REFRESH_TOKEN_NOT_FOUND);
        }
        if (!stored.equals(refreshToken)) {
            throw new BusinessException(INVALID_REFRESH_TOKEN);
        }

        // 4. 유저 조회 후 새 Access Token 발급
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        return jwtProvider.generateAccessToken(user.getUserId(), user.getRole().name());
    }

    public void logout() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    public UserProfileResponse getProfile() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
        return UserProfileResponse.from(user);
    }
}
