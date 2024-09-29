package com.khantech.assignment.controller;

import com.khantech.assignment.dto.CreateWalletRequest;
import com.khantech.assignment.dto.SubmitTransactionRequest;
import com.khantech.assignment.dto.Transaction;
import com.khantech.assignment.dto.Wallet;
import com.khantech.assignment.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<Wallet> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        Wallet wallet = walletService.createWallet(request);
        return new ResponseEntity<>(wallet, HttpStatus.CREATED);
    }

    @PostMapping("/{walletId}/transactions")
    public ResponseEntity<Transaction> submitTransaction(@PathVariable UUID walletId,
                                                         @Valid @RequestBody SubmitTransactionRequest request) {

        Transaction transaction = walletService.submitTransaction(walletId, request);

        return new ResponseEntity<>(transaction, HttpStatus.ACCEPTED);
    }

}