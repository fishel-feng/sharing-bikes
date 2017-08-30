package com.fx.sharingbikes.wallet.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Wallet {
    private Long id;

    private Long userId;

    private BigDecimal remainSum;

    private BigDecimal deposit;
}