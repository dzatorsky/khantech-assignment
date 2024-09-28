package com.khantech.assignment.service;

import com.khantech.assignment.dto.CreateWalletDTO;
import com.khantech.assignment.dto.WalletDTO;
import com.khantech.assignment.entity.UserEntity;
import com.khantech.assignment.entity.WalletEntity;
import com.khantech.assignment.error.UserNotFoundException;
import com.khantech.assignment.error.WalletAlreadyExistsException;
import com.khantech.assignment.repository.UserRepository;
import com.khantech.assignment.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class WalletService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public WalletDTO createWallet(CreateWalletDTO dto) {

        UserEntity user = this.userRepository
                .findById(dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException(dto.getUserId()));

        this.walletRepository
                .findByUserIdAndCurrency(dto.getUserId(), dto.getCurrency())
                .ifPresent(wallet -> {
                    throw new WalletAlreadyExistsException(dto.getUserId(), dto.getCurrency());
                });

        WalletEntity wallet = new WalletEntity();
        wallet.setUser(user);
        wallet.setCurrency(dto.getCurrency());
        wallet.setBalance(BigDecimal.ZERO);

        this.walletRepository.save(wallet);

        WalletDTO walletDTO = new WalletDTO();
        walletDTO.setId(wallet.getId());
        walletDTO.setUserId(user.getId());
        walletDTO.setCurrency(wallet.getCurrency());
        walletDTO.setBalance(wallet.getBalance());

        return walletDTO;
    }
}
