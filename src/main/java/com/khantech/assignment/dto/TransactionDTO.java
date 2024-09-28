package com.khantech.assignment.dto;

import com.khantech.assignment.enums.TransactionStatus;
import com.khantech.assignment.enums.TransactionType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TransactionDTO {
    private UUID id;

    private UUID userId;

    private UUID walletId;

    private BigDecimal amount;

    private TransactionType type;

    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;

    private TransactionStatus status;

    private Instant createdAt;
}
