package com.khantech.assignment.controller;

import com.khantech.assignment.dto.CreateWalletDTO;
import com.khantech.assignment.dto.SubmitTransactionDTO;
import com.khantech.assignment.dto.TransactionDTO;
import com.khantech.assignment.dto.WalletDTO;
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
    public ResponseEntity<WalletDTO> createWallet(@Valid @RequestBody CreateWalletDTO dto) {
        WalletDTO walletDTO = walletService.createWallet(dto);
        return new ResponseEntity<>(walletDTO, HttpStatus.CREATED);
    }

    @PostMapping("/{walletId}/transactions")
    public ResponseEntity<TransactionDTO> submitTransaction(@PathVariable UUID walletId,
                                                            @Valid @RequestBody SubmitTransactionDTO dto) {

        TransactionDTO transactionDTO = walletService.submitTransaction(walletId, dto);

        return new ResponseEntity<>(transactionDTO, HttpStatus.ACCEPTED);
    }

}