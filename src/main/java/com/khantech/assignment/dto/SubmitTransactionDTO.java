package com.khantech.assignment.dto;

import com.khantech.assignment.enums.TransactionType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class SubmitTransactionDTO {
    private UUID userId;

    private String currency;

    private BigDecimal amount;

    private TransactionType type;
}
