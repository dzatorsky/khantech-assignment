package com.khantech.assignment.error;

import com.khantech.assignment.enums.TransactionStatus;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class InvalidTransactionStateException extends CommonException {

    public InvalidTransactionStateException(UUID transactionId, TransactionStatus currentStatus, TransactionStatus expectedStatus) {
        super("invalid-transaction-state", "Transaction with id " + transactionId +
                                           " is expected to be in status " + expectedStatus +
                                           " but the actual status is " + currentStatus, HttpStatus.CONFLICT);
    }

}
