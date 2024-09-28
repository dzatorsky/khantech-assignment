package com.khantech.assignment.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class CreateWalletDTO {
    private UUID userId;
    private String currency;
}
