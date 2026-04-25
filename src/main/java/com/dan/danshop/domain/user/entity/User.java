package com.dan.danshop.domain.user.entity;

import com.dan.danshop.domain.user.dto.SignupRequest;
import com.dan.danshop.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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


    public static User from(SignupRequest request, String encodedPassword) {
        return User.builder()
                .userId(request.getUserId())
                .name(request.getName())
                .email(request.getEmail())
                .password(encodedPassword)
                .build();
    }
}
