package com.dan.danshop.domain.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Orderer {

    @Column(name = "orderer_name")
    private String name;

    @Column(name = "orderer_email")
    private String email;

    @Column(name = "orderer_phone")
    private String phone;
}
