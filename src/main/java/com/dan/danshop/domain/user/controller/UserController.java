package com.dan.danshop.domain.user.controller;

import com.dan.danshop.domain.user.dto.LoginRequest;
import com.dan.danshop.domain.user.dto.SignupRequest;
import com.dan.danshop.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "User", description = "회원 관련 API")
public class UserController {

    private final UserService userService;

    @PostMapping(value = "/signup")
    @Operation(summary = "회원가입")
    public ResponseEntity<?> signUp(@RequestBody SignupRequest signupRequest) {

        userService.userSignup(signupRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 완료");
    }

    @PostMapping(value = "/login")
    @Operation(summary = "로그인")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String token = userService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).body(token);
    }
}
