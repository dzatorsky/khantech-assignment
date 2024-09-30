package com.khantech.assignment.service.handler.approval.impl;

import com.khantech.assignment.entity.TransactionEntity;
import com.khantech.assignment.entity.WalletEntity;
import com.khantech.assignment.enums.TransactionType;
import com.khantech.assignment.error.InsufficientFundsException;
import com.khantech.assignment.service.handler.approval.ApproveTransactionContext;
import com.khantech.assignment.service.handler.approval.ApproveTransactionHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Order(1)
@Component
@Transactional
@RequiredArgsConstructor
public class ApproveTransactionBalanceHandler implements ApproveTransactionHandler {

    @Override
    public void handle(ApproveTransactionContext context) {
        TransactionEntity transaction = context.getTransaction();
        WalletEntity wallet = transaction.getWallet();

        if (transaction.getType() == TransactionType.DEBIT) {
            if (transaction.getAmount().compareTo(wallet.getBalance()) > 0) {
                throw new InsufficientFundsException(wallet.getId(), wallet.getBalance());
            }
        }

        transaction.setBalanceBefore(wallet.getBalance());

        BigDecimal transactionAmount = getTransactionAmount(transaction.getType(), transaction.getAmount());
        BigDecimal newBalance = wallet.getBalance().add(transactionAmount);

        wallet.setBalance(newBalance);
        transaction.setBalanceAfter(newBalance);
    }

    private BigDecimal getTransactionAmount(TransactionType transactionType, BigDecimal transactionAmount) {
        BigDecimal amount = transactionAmount;
        if (transactionType == TransactionType.DEBIT) {
            amount = transactionAmount.negate();
        }
        return amount;
    }
}
