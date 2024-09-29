package com.khantech.assignment.service.handler.submission;

import com.khantech.assignment.dto.SubmitTransactionRequest;

public interface SubmitTransactionHandler {
    void handle(SubmitTransactionContext context, SubmitTransactionRequest request);
}
