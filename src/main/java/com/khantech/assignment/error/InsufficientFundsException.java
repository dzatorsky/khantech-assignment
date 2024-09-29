package com.khantech.assignment.error;

import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientFundsException extends CommonException {

    public InsufficientFundsException(UUID walletId, BigDecimal availableBalance) {
        super("insufficient-funds", "Insufficient funds for wallet with  id " + walletId +
                                    ". Available balance is " + availableBalance, HttpStatus.BAD_REQUEST);
    }

}
