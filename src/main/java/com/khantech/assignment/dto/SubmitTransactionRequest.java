package com.khantech.assignment.dto;

import com.khantech.assignment.enums.TransactionType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class SubmitTransactionRequest {

    @NotNull
    private UUID requestId;

    @NotNull
    @Positive
    @Digits(integer = 19, fraction = 2)
    private BigDecimal amount;

    @NotNull
    private TransactionType type;

}
