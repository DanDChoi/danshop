package com.dan.danshop.domain.user.service;

import com.dan.danshop.domain.coupon.repository.UserCouponRepository;
import com.dan.danshop.domain.order.repository.OrderItemRepository;
import com.dan.danshop.domain.order.repository.OrderRepository;
import com.dan.danshop.domain.point.repository.PointHistoryRepository;
import com.dan.danshop.domain.review.repository.ReviewRepository;
import com.dan.danshop.domain.user.dto.ChangePasswordRequest;
import com.dan.danshop.domain.user.dto.LoginRequest;
import com.dan.danshop.domain.user.dto.SignupRequest;
import com.dan.danshop.domain.user.dto.TokenResponse;
import com.dan.danshop.domain.user.dto.UserProfileResponse;
import com.dan.danshop.domain.user.repository.UserRepository;
import com.dan.danshop.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class UserServiceTest {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private PointHistoryRepository pointHistoryRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private UserCouponRepository userCouponRepository;

    @BeforeEach
    void setUp() {
        pointHistoryRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        reviewRepository.deleteAll();
        userCouponRepository.deleteAll();
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    @Test
    void 회원가입_후_로그인이_가능하다() {
        // given
        SignupRequest signup = new SignupRequest();
        ReflectionTestUtils.setField(signup, "userId", "testuser1");
        ReflectionTestUtils.setField(signup, "password", "password123");
        ReflectionTestUtils.setField(signup, "name", "테스트유저");
        ReflectionTestUtils.setField(signup, "email", "test1@test.com");

        // when
        userService.userSignup(signup);

        LoginRequest login = new LoginRequest();
        ReflectionTestUtils.setField(login, "userId", "testuser1");
        ReflectionTestUtils.setField(login, "password", "password123");
        TokenResponse token = userService.login(login);

        // then
        assertThat(token.getAccessToken()).isNotBlank();
        assertThat(token.getRefreshToken()).isNotBlank();

        System.out.println("로그인 성공, accessToken 발급 완료");
    }

    @Test
    void 중복_아이디로_회원가입시_예외가_발생한다() {
        // given
        SignupRequest signup = new SignupRequest();
        ReflectionTestUtils.setField(signup, "userId", "dupuser");
        ReflectionTestUtils.setField(signup, "password", "password123");
        ReflectionTestUtils.setField(signup, "name", "중복유저");
        ReflectionTestUtils.setField(signup, "email", "dup@test.com");

        userService.userSignup(signup);

        // then
        assertThatThrownBy(() -> userService.userSignup(signup))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 존재하는 아이디입니다.");

        System.out.println("중복 아이디 예외 확인 완료");
    }

    @Test
    void 잘못된_비밀번호로_로그인시_예외가_발생한다() {
        // given
        SignupRequest signup = new SignupRequest();
        ReflectionTestUtils.setField(signup, "userId", "logintest");
        ReflectionTestUtils.setField(signup, "password", "correctpassword");
        ReflectionTestUtils.setField(signup, "name", "로그인테스터");
        ReflectionTestUtils.setField(signup, "email", "login@test.com");
        userService.userSignup(signup);

        LoginRequest login = new LoginRequest();
        ReflectionTestUtils.setField(login, "userId", "logintest");
        ReflectionTestUtils.setField(login, "password", "wrongpassword");

        // then
        assertThatThrownBy(() -> userService.login(login))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다.");

        System.out.println("잘못된 비밀번호 예외 확인 완료");
    }

    @Test
    void 비밀번호_변경_후_새_비밀번호로_로그인이_가능하다() {
        // given
        SignupRequest signup = new SignupRequest();
        ReflectionTestUtils.setField(signup, "userId", "pwchange");
        ReflectionTestUtils.setField(signup, "password", "oldpassword1");
        ReflectionTestUtils.setField(signup, "name", "비번변경유저");
        ReflectionTestUtils.setField(signup, "email", "pw@test.com");
        userService.userSignup(signup);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("pwchange", null, List.of())
        );

        ChangePasswordRequest changeReq = new ChangePasswordRequest();
        ReflectionTestUtils.setField(changeReq, "currentPassword", "oldpassword1");
        ReflectionTestUtils.setField(changeReq, "newPassword", "newpassword1");

        // when
        userService.changePassword(changeReq);

        // then - 새 비밀번호로 로그인 가능
        LoginRequest login = new LoginRequest();
        ReflectionTestUtils.setField(login, "userId", "pwchange");
        ReflectionTestUtils.setField(login, "password", "newpassword1");
        TokenResponse token = userService.login(login);
        assertThat(token.getAccessToken()).isNotBlank();

        System.out.println("비밀번호 변경 후 새 비밀번호 로그인 성공");
    }

    @Test
    void 현재_비밀번호_불일치시_변경이_실패한다() {
        // given
        SignupRequest signup = new SignupRequest();
        ReflectionTestUtils.setField(signup, "userId", "pwfail");
        ReflectionTestUtils.setField(signup, "password", "correctpw1");
        ReflectionTestUtils.setField(signup, "name", "비번실패유저");
        ReflectionTestUtils.setField(signup, "email", "pwfail@test.com");
        userService.userSignup(signup);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("pwfail", null, List.of())
        );

        ChangePasswordRequest changeReq = new ChangePasswordRequest();
        ReflectionTestUtils.setField(changeReq, "currentPassword", "wrongpw123");
        ReflectionTestUtils.setField(changeReq, "newPassword", "newpassword1");

        // then
        assertThatThrownBy(() -> userService.changePassword(changeReq))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다.");

        System.out.println("현재 비밀번호 불일치 예외 확인 완료");
    }

    @Test
    void 프로필_조회시_유저_정보가_반환된다() {
        // given
        SignupRequest signup = new SignupRequest();
        ReflectionTestUtils.setField(signup, "userId", "profileuser");
        ReflectionTestUtils.setField(signup, "password", "password123");
        ReflectionTestUtils.setField(signup, "name", "프로필유저");
        ReflectionTestUtils.setField(signup, "email", "profile@test.com");
        userService.userSignup(signup);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("profileuser", null, List.of())
        );

        // when
        UserProfileResponse profile = userService.getProfile();

        // then
        assertThat(profile.getUserId()).isEqualTo("profileuser");
        assertThat(profile.getName()).isEqualTo("프로필유저");
        assertThat(profile.getEmail()).isEqualTo("profile@test.com");
        assertThat(profile.getPointBalance()).isEqualTo(0L);

        System.out.println("프로필 조회 완료: " + profile.getUserId());
    }
}
