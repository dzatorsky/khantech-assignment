package com.khantech.assignment.repository;

import com.khantech.assignment.entity.TransactionEntity;
import com.khantech.assignment.enums.TransactionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {
    List<TransactionEntity> findAllByStatusAndCreatedAtBefore(TransactionStatus status, Instant timeoutDate, Pageable pageable);

    Optional<TransactionEntity> findByRequestId(UUID requestId);
}