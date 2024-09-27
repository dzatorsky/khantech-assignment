package com.khantech.assignment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user")
@Accessors(chain = true)
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String name;

    @OneToMany(mappedBy = "user")
    private List<WalletEntity> wallets;

    @OneToMany(mappedBy = "user")
    private List<TransactionEntity> transactions;
}