package com.khantech.assignment.error;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class DuplicatedTransactionException extends CommonException {

    public DuplicatedTransactionException(UUID requestId) {
        super("duplicated-transaction", "Transaction with requestId=" + requestId + " already exists", HttpStatus.CONFLICT);
    }

}
