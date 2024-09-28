package com.khantech.assignment.dto;

import com.khantech.assignment.enums.TransactionType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class SubmitTransactionDTO {
    private BigDecimal amount;
    private TransactionType type;
}
