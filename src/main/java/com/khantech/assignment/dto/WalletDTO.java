package com.khantech.assignment.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class WalletDTO {
    private UUID id;

    private UUID userId;

    private String currency;

    private BigDecimal balance;
}
