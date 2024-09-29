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
import com.khantech.assignment.error.*;
import com.khantech.assignment.repository.TransactionRepository;
import com.khantech.assignment.repository.UserRepository;
import com.khantech.assignment.repository.WalletRepository;
import com.khantech.assignment.service.handler.approval.ApproveTransactionContext;
import com.khantech.assignment.service.handler.approval.ApproveTransactionHandler;
import com.khantech.assignment.service.handler.submission.SubmitTransactionContext;
import com.khantech.assignment.service.handler.submission.SubmitTransactionHandler;
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
    private final List<SubmitTransactionHandler> submitTransactionHandlers;
    private final List<ApproveTransactionHandler> approveTransactionHandlers;

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

        TransactionEntity transaction = new TransactionEntity()
                .setRequestId(request.getRequestId())
                .setUser(wallet.getUser())
                .setWallet(wallet)
                .setAmount(request.getAmount())
                .setBalanceBefore(wallet.getBalance())
                .setType(request.getType())
                .setCreatedAt(Instant.now());

        submitTransactionHandlers.forEach(step -> step.handle(new SubmitTransactionContext(transaction), request));

        walletRepository.save(wallet);
        transactionRepository.save(transaction);

        return new Transaction()
                .setId(transaction.getId())
                .setRequestId(transaction.getRequestId())
                .setUserId(wallet.getUser().getId())
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

        approveTransactionHandlers.forEach(step -> step.handle(new ApproveTransactionContext(transaction)));

        walletRepository.save(transaction.getWallet());
        transactionRepository.save(transaction);
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

}
