package com.dan.danshop.domain.user.dto;

import com.dan.danshop.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileResponse {
    private String userId;
    private String name;
    private String email;
    private Long pointBalance;

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getPointBalance()
        );
    }
}
