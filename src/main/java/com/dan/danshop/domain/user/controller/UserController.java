package com.dan.danshop.domain.user.controller;

import com.dan.danshop.domain.user.dto.ChangePasswordRequest;
import com.dan.danshop.domain.user.dto.LoginRequest;
import com.dan.danshop.domain.user.dto.RefreshRequest;
import com.dan.danshop.domain.user.dto.SignupRequest;
import com.dan.danshop.domain.user.dto.TokenResponse;
import com.dan.danshop.domain.user.dto.UserProfileResponse;
import com.dan.danshop.domain.user.service.UserService;
import com.dan.danshop.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
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
    @ApiResponse(responseCode = "201", description = "회원가입 성공")
    @ApiResponse(responseCode = "409", description = "이미 존재하는 아이디",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> signUp(@Valid @RequestBody SignupRequest signupRequest) {

        userService.userSignup(signupRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 완료");
    }

    @PostMapping(value = "/login")
    @Operation(summary = "로그인")
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @ApiResponse(responseCode = "401", description = "비밀번호 불일치",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "사용자 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(userService.login(loginRequest));
    }

    @PostMapping(value = "/refresh")
    @Operation(summary = "Access Token 재발급")
    @ApiResponse(responseCode = "200", description = "재발급 성공")
    @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<String> refresh(@RequestBody RefreshRequest refreshRequest) {
        return ResponseEntity.ok(userService.refresh(refreshRequest));
    }

    @PostMapping(value = "/logout")
    @Operation(summary = "로그아웃")
    public ResponseEntity<String> logout() {
        userService.logout();
        return ResponseEntity.ok("로그아웃 완료");
    }

    @GetMapping(value = "/me")
    @Operation(summary = "내 프로필 조회")
    public ResponseEntity<UserProfileResponse> getProfile() {
        return ResponseEntity.ok(userService.getProfile());
    }

    @PutMapping(value = "/password")
    @Operation(summary = "비밀번호 변경")
    @ApiResponse(responseCode = "200", description = "변경 성공")
    @ApiResponse(responseCode = "401", description = "현재 비밀번호 불일치",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }
}
