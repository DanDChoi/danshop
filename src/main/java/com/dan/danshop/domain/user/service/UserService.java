package com.dan.danshop.domain.user.service;

import com.dan.danshop.domain.user.dto.LoginRequest;
import com.dan.danshop.domain.user.dto.SignupRequest;
import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.domain.user.repository.UserRepository;
import com.dan.danshop.global.config.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public void userSignup(SignupRequest signupRequest) {
        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());
        User newUser = User.from(signupRequest, encodedPassword);
        userRepository.save(newUser);
    }

    public String login(LoginRequest loginRequest) {

        User existsUser = userRepository.findByUserId(loginRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디입니다"));

        if (passwordEncoder.matches(loginRequest.getPassword(), existsUser.getPassword())) {
            String token = jwtProvider.generateToken(existsUser.getUserId());
            return token;
        } else {
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }

    }
}
