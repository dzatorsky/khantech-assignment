package com.khantech.assignment.service;

import com.khantech.assignment.config.AppProperties;
import com.khantech.assignment.dto.CreateWalletDTO;
import com.khantech.assignment.dto.SubmitTransactionDTO;
import com.khantech.assignment.dto.TransactionDTO;
import com.khantech.assignment.dto.WalletDTO;
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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
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

    public WalletDTO createWallet(CreateWalletDTO dto) {

        UserEntity user = this.userRepository
                .findById(dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException(dto.getUserId()));

        this.walletRepository
                .findByUserIdAndCurrency(dto.getUserId(), dto.getCurrency())
                .ifPresent(_ -> {
                    throw new WalletAlreadyExistsException(dto.getUserId(), dto.getCurrency());
                });

        WalletEntity wallet = new WalletEntity()
                .setUser(user)
                .setCurrency(dto.getCurrency())
                .setBalance(BigDecimal.ZERO);

        this.walletRepository.save(wallet);

        return new WalletDTO()
                .setId(wallet.getId())
                .setUserId(user.getId())
                .setCurrency(wallet.getCurrency())
                .setBalance(wallet.getBalance());
    }

    public TransactionDTO submitTransaction(UUID walletId, SubmitTransactionDTO submitDTO) {
        WalletEntity wallet = this.walletRepository
                .findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        UserEntity user = wallet.getUser();

        TransactionEntity transaction = new TransactionEntity()
                .setUser(user)
                .setWallet(wallet)
                .setAmount(submitDTO.getAmount())
                .setBalanceBefore(wallet.getBalance())
                .setType(submitDTO.getType())
                .setCreatedAt(Instant.now());

        if (isWithinThreshold(submitDTO.getAmount())) {
            transaction.setStatus(TransactionStatus.APPROVED);
            transaction.setBalanceAfter(wallet.getBalance().add(getTransactionAmount(submitDTO.getType(), submitDTO.getAmount())));
            wallet.setBalance(wallet.getBalance().add(getTransactionAmount(submitDTO.getType(), submitDTO.getAmount())));
        } else {
            transaction.setStatus(TransactionStatus.AWAITING_APPROVAL);
            transaction.setBalanceAfter(wallet.getBalance());
        }

        verifyEnoughBalance(wallet);

        walletRepository.save(wallet);
        transactionRepository.save(transaction);

        return new TransactionDTO()
                .setId(transaction.getId())
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
