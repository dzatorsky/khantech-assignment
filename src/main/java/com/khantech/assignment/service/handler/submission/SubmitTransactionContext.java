package com.khantech.assignment.service.handler.submission;

import com.khantech.assignment.entity.TransactionEntity;
import lombok.Data;

@Data
public class SubmitTransactionContext {
    private final TransactionEntity transaction;
}
