package com.khantech.assignment.service.handler.submission.impl;

import com.khantech.assignment.dto.SubmitTransactionRequest;
import com.khantech.assignment.entity.WalletEntity;
import com.khantech.assignment.enums.TransactionType;
import com.khantech.assignment.error.InsufficientFundsException;
import com.khantech.assignment.service.handler.submission.SubmitTransactionContext;
import com.khantech.assignment.service.handler.submission.SubmitTransactionHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(2)
@Component
@Transactional
@RequiredArgsConstructor
public class SubmitTransactionBalanceHandler implements SubmitTransactionHandler {

    @Override
    public void handle(SubmitTransactionContext context, SubmitTransactionRequest request) {
        WalletEntity wallet = context.getTransaction().getWallet();

        if (request.getType() == TransactionType.DEBIT) {
            if (request.getAmount().compareTo(wallet.getBalance()) > 0) {
                throw new InsufficientFundsException(wallet.getId(), wallet.getBalance());
            }
        }

    }
}
