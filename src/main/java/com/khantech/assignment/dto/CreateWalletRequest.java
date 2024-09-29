package com.khantech.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class CreateWalletRequest {

    @NotNull
    private UUID userId;

    @NotBlank
    private String currency;

}
