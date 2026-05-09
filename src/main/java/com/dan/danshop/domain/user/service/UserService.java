package com.dan.danshop.domain.user.service;

import com.dan.danshop.domain.user.dto.LoginRequest;
import com.dan.danshop.domain.user.dto.SignupRequest;
import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.domain.user.repository.UserRepository;
import com.dan.danshop.global.config.JwtProvider;
import com.dan.danshop.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import static com.dan.danshop.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public void userSignup(SignupRequest signupRequest) {
        //중복체크
        if (userRepository.findByUserId(signupRequest.getUserId()).isPresent()) {
            throw new BusinessException(DUPLICATED_USER_ID);
        }
        //비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());
        //Entity 생성
        User newUser = User.from(signupRequest, encodedPassword);
        //저장
        userRepository.save(newUser);
    }

    public String login(LoginRequest loginRequest) {

        User existsUser = userRepository.findByUserId(loginRequest.getUserId())
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        if (passwordEncoder.matches(loginRequest.getPassword(), existsUser.getPassword())) {
            String token = jwtProvider.generateToken(existsUser.getUserId());
            return token;
        } else {
            throw new BusinessException(PASSWORD_NOT_MATCH);
        }

    }
}
