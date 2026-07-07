package com.dan.danshop.domain.user.entity;

import com.dan.danshop.domain.user.dto.SignupRequest;
import com.dan.danshop.global.common.BaseEntity;
import com.dan.danshop.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import static com.dan.danshop.global.exception.ErrorCode.INSUFFICIENT_POINTS;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    private String email;
    private String password;
    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long pointBalance = 0L;

    public void addPoints(long amount) {
        this.pointBalance += amount;
    }

    public void deductPoints(long amount) {
        if (this.pointBalance < amount) throw new BusinessException(INSUFFICIENT_POINTS);
        this.pointBalance -= amount;
    }

    public void forceDeductPoints(long amount) {
        this.pointBalance = Math.max(0L, this.pointBalance - amount);
    }

    public void changePassword(String encodedNewPassword) {
        this.password = encodedNewPassword;
    }

    public static User from(SignupRequest request, String encodedPassword) {
        return User.builder()
                .userId(request.getUserId())
                .name(request.getName())
                .email(request.getEmail())
                .password(encodedPassword)
                .role(Role.ROLE_USER)
                .build();
    }
}
