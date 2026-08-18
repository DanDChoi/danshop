package com.dan.danshop.domain.order.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GuestOrderLookupRequest {

    @NotNull
    private Long orderId;

    @NotBlank
    @Email
    private String email;
}
