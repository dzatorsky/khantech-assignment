package com.khantech.assignment.dto;

import com.khantech.assignment.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class SubmitTransactionDTO {

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private TransactionType type;

}
