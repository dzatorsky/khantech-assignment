package com.khantech.assignment.error;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class WalletNotFoundException extends CommonException {

    public WalletNotFoundException(UUID walletId) {
        super("wallet-not-found", "Wallet with id " + walletId + " could not be found", HttpStatus.NOT_FOUND);
    }

}
