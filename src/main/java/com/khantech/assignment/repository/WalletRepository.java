package com.khantech.assignment.repository;

import com.khantech.assignment.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<WalletEntity, UUID> {
    Optional<WalletEntity> findByUserIdAndCurrency(UUID userId, String currency);
}