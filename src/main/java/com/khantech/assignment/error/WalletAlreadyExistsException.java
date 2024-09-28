package com.khantech.assignment.error;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class WalletAlreadyExistsException extends CommonException {

    public WalletAlreadyExistsException(UUID userId, String currency) {
        super("wallet-already-exists", "Wallet already exists for user " + userId + " and currency " + currency, HttpStatus.BAD_REQUEST);
    }

}
