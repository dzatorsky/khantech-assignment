package com.khantech.assignment.service.handler.submission.impl;

import com.khantech.assignment.config.AppProperties;
import com.khantech.assignment.dto.SubmitTransactionRequest;
import com.khantech.assignment.entity.TransactionEntity;
import com.khantech.assignment.entity.WalletEntity;
import com.khantech.assignment.enums.TransactionStatus;
import com.khantech.assignment.enums.TransactionType;
import com.khantech.assignment.service.handler.submission.SubmitTransactionContext;
import com.khantech.assignment.service.handler.submission.SubmitTransactionHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Transactional
@RequiredArgsConstructor
public class SubmitTransactionApprovalHandler implements SubmitTransactionHandler {

    private final AppProperties appProperties;

    @Override
    public void handle(SubmitTransactionContext context, SubmitTransactionRequest request) {
        TransactionEntity transaction = context.getTransaction();
        WalletEntity wallet = transaction.getWallet();

        if (isWithinThreshold(request.getAmount())) {
            transaction.setStatus(TransactionStatus.APPROVED);
            transaction.setBalanceAfter(wallet.getBalance().add(getTransactionAmount(request.getType(), request.getAmount())));
            wallet.setBalance(wallet.getBalance().add(getTransactionAmount(request.getType(), request.getAmount())));
        } else {
            transaction.setStatus(TransactionStatus.AWAITING_APPROVAL);
            transaction.setBalanceAfter(wallet.getBalance());
        }
    }

    private boolean isWithinThreshold(BigDecimal amount) {
        BigDecimal threshold = appProperties.getWallet().getTransaction().getThreshold();
        return threshold.compareTo(amount) > 0;
    }

    private BigDecimal getTransactionAmount(TransactionType transactionType, BigDecimal transactionAmount) {
        BigDecimal amount = transactionAmount;
        if (transactionType == TransactionType.DEBIT) {
            amount = transactionAmount.negate();
        }
        return amount;
    }
}
