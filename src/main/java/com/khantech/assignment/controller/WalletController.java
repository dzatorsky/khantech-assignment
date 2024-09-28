package com.khantech.assignment.controller;

import com.khantech.assignment.dto.CreateWalletDTO;
import com.khantech.assignment.dto.WalletDTO;
import com.khantech.assignment.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<WalletDTO> createWallet(@RequestBody CreateWalletDTO dto) {
        WalletDTO walletDTO = walletService.createWallet(dto);
        return new ResponseEntity<>(walletDTO, HttpStatus.CREATED);
    }

}