package com.khantech.assignment.service;

import com.khantech.assignment.config.AppProperties;
import com.khantech.assignment.dto.CreateWalletRequest;
import com.khantech.assignment.dto.SubmitTransactionRequest;
import com.khantech.assignment.dto.Transaction;
import com.khantech.assignment.dto.Wallet;
import com.khantech.assignment.entity.TransactionEntity;
import com.khantech.assignment.entity.UserEntity;
import com.khantech.assignment.entity.WalletEntity;
import com.khantech.assignment.enums.TransactionStatus;
import com.khantech.assignment.enums.TransactionType;
import com.khantech.assignment.error.*;
import com.khantech.assignment.repository.TransactionRepository;
import com.khantech.assignment.repository.UserRepository;
import com.khantech.assignment.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class WalletService {

    private final AppProperties appProperties;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public Wallet createWallet(CreateWalletRequest request) {

        UserEntity user = this.userRepository
                .findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException(request.getUserId()));

        this.walletRepository
                .findByUserIdAndCurrency(request.getUserId(), request.getCurrency())
                .ifPresent(_ -> {
                    throw new WalletAlreadyExistsException(request.getUserId(), request.getCurrency());
                });

        WalletEntity wallet = new WalletEntity()
                .setUser(user)
                .setCurrency(request.getCurrency())
                .setBalance(BigDecimal.ZERO);

        this.walletRepository.save(wallet);

        return new Wallet()
                .setId(wallet.getId())
                .setUserId(user.getId())
                .setCurrency(wallet.getCurrency())
                .setBalance(wallet.getBalance());
    }

    public Transaction submitTransaction(UUID walletId, SubmitTransactionRequest request) {
        WalletEntity wallet = this.walletRepository
                .findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        transactionRepository
                .findByRequestId(request.getRequestId())
                .ifPresent(transaction -> {
                    throw new DuplicatedTransactionException(transaction.getRequestId());
                });

        UserEntity user = wallet.getUser();

        TransactionEntity transaction = new TransactionEntity()
                .setRequestId(request.getRequestId())
                .setUser(user)
                .setWallet(wallet)
                .setAmount(request.getAmount())
                .setBalanceBefore(wallet.getBalance())
                .setType(request.getType())
                .setCreatedAt(Instant.now());

        if (isWithinThreshold(request.getAmount())) {
            transaction.setStatus(TransactionStatus.APPROVED);
            transaction.setBalanceAfter(wallet.getBalance().add(getTransactionAmount(request.getType(), request.getAmount())));
            wallet.setBalance(wallet.getBalance().add(getTransactionAmount(request.getType(), request.getAmount())));
        } else {
            transaction.setStatus(TransactionStatus.AWAITING_APPROVAL);
            transaction.setBalanceAfter(wallet.getBalance());
        }

        verifyEnoughBalance(wallet);

        walletRepository.save(wallet);
        transactionRepository.save(transaction);

        return new Transaction()
                .setId(transaction.getId())
                .setRequestId(transaction.getRequestId())
                .setUserId(user.getId())
                .setWalletId(wallet.getId())
                .setAmount(transaction.getAmount())
                .setType(transaction.getType())
                .setBalanceBefore(transaction.getBalanceBefore())
                .setBalanceAfter(transaction.getBalanceAfter())
                .setStatus(transaction.getStatus())
                .setCreatedAt(transaction.getCreatedAt());
    }

    public void approveTransaction(UUID transactionId) {
        TransactionEntity transaction = this.transactionRepository
                .findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        if (transaction.getStatus() == TransactionStatus.AWAITING_APPROVAL) {
            WalletEntity wallet = transaction.getWallet();

            transaction.setStatus(TransactionStatus.APPROVED);
            transaction.setBalanceBefore(wallet.getBalance());

            BigDecimal transactionAmount = getTransactionAmount(transaction.getType(), transaction.getAmount());
            BigDecimal newBalance = wallet.getBalance().add(transactionAmount);

            wallet.setBalance(newBalance);
            transaction.setBalanceAfter(newBalance);

            verifyEnoughBalance(wallet);

            walletRepository.save(wallet);
            transactionRepository.save(transaction);
        } else {
            throw new InvalidTransactionStateException(transactionId, transaction.getStatus(), TransactionStatus.AWAITING_APPROVAL);
        }
    }

    public void rejectExpiredTransactions(Integer batchSize) {
        Instant expirationDate = Instant.now().minus(appProperties.getWallet().getTransaction().getApprovalTimeout());

        Pageable pageable = PageRequest.of(0, batchSize, Sort.by(Sort.Direction.ASC, TransactionEntity.Fields.createdAt));

        List<TransactionEntity> rejectedTransactions = this.transactionRepository
                .findAllByStatusAndCreatedAtBefore(TransactionStatus.AWAITING_APPROVAL, expirationDate, pageable)
                .stream()
                .map(transaction -> {
                    if (transaction.getStatus() == TransactionStatus.AWAITING_APPROVAL) {
                        transaction.setStatus(TransactionStatus.REJECTED);
                        return transaction;
                    } else {
                        throw new InvalidTransactionStateException(transaction.getId(), transaction.getStatus(), TransactionStatus.AWAITING_APPROVAL);
                    }
                })
                .toList();

        transactionRepository.saveAll(rejectedTransactions);
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

    private void verifyEnoughBalance(WalletEntity wallet) {
        if (wallet.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InsufficientFundsException(wallet.getId(), wallet.getBalance());
        }
    }
}
