package com.khantech.assignment.entity;

import com.khantech.assignment.enums.TransactionStatus;
import com.khantech.assignment.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@FieldNameConstants
@Accessors(chain = true)
@Table(name = "transaction")
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private WalletEntity wallet;

    @Column
    private BigDecimal amount;

    @Column
    private BigDecimal balanceBefore;

    @Column
    private BigDecimal balanceAfter;

    @Column
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column
    private Instant createdAt;
}