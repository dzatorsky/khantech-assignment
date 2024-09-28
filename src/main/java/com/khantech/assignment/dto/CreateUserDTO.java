package com.khantech.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CreateUserDTO {
    @NotBlank
    private String name;
}