package com.khantech.assignment.controller;

import com.khantech.assignment.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/admin/transactions")
@RequiredArgsConstructor
public class AdminController {

    private final WalletService walletService;

    @PostMapping("/{transactionId}/approve")
    public void approveTransaction(@PathVariable UUID transactionId) {
        walletService.approveTransaction(transactionId);
    }

}