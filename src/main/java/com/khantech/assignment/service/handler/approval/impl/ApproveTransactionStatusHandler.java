package com.khantech.assignment.service.handler.approval.impl;

import com.khantech.assignment.entity.TransactionEntity;
import com.khantech.assignment.enums.TransactionStatus;
import com.khantech.assignment.error.InvalidTransactionStateException;
import com.khantech.assignment.service.handler.approval.ApproveTransactionContext;
import com.khantech.assignment.service.handler.approval.ApproveTransactionHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(2)
@Component
@Transactional
@RequiredArgsConstructor
public class ApproveTransactionStatusHandler implements ApproveTransactionHandler {

    @Override
    public void handle(ApproveTransactionContext context) {
        TransactionEntity transaction = context.getTransaction();
        if (transaction.getStatus() != TransactionStatus.AWAITING_APPROVAL) {
            throw new InvalidTransactionStateException(transaction.getId(), transaction.getStatus(), TransactionStatus.AWAITING_APPROVAL);
        } else {
            transaction.setStatus(TransactionStatus.APPROVED);
        }
    }
}
