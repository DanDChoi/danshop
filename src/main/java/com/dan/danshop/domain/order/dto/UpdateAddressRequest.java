package com.dan.danshop.domain.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateAddressRequest {

    @NotBlank(message = "우편번호를 입력해주세요.")
    private String postNo;

    @NotBlank(message = "기본 주소를 입력해주세요.")
    private String baseAddr;

    @NotBlank(message = "상세 주소를 입력해주세요.")
    private String detailAddr;
}
