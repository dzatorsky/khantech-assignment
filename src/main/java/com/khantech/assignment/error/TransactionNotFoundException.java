package com.khantech.assignment.error;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class TransactionNotFoundException extends CommonException {

    public TransactionNotFoundException(UUID transactionId) {
        super("transaction-not-found", "Transaction with id " + transactionId + " could not be found", HttpStatus.NOT_FOUND);
    }

}
