package com.khantech.assignment.service.handler.approval;

import com.khantech.assignment.entity.TransactionEntity;
import lombok.Data;

@Data
public class ApproveTransactionContext {
    private final TransactionEntity transaction;
}
