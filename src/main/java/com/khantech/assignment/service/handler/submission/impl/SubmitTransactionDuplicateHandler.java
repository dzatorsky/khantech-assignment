package com.khantech.assignment.service.handler.submission.impl;

import com.khantech.assignment.dto.SubmitTransactionRequest;
import com.khantech.assignment.error.DuplicatedTransactionException;
import com.khantech.assignment.repository.TransactionRepository;
import com.khantech.assignment.service.handler.submission.SubmitTransactionContext;
import com.khantech.assignment.service.handler.submission.SubmitTransactionHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
@Component
@Transactional
@RequiredArgsConstructor
public class SubmitTransactionDuplicateHandler implements SubmitTransactionHandler {

    private final TransactionRepository repository;

    @Override
    public void handle(SubmitTransactionContext context, SubmitTransactionRequest request) {
        repository
                .findByRequestId(request.getRequestId())
                .ifPresent(transaction -> {
                    throw new DuplicatedTransactionException(transaction.getRequestId());
                });
    }
}
