package com.khantech.assignment.error.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.List;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonErrorResponse {

    private String code;

    private String message;

    private Integer status;

    private String details;

    private String origin;

    private Instant time;

    private List<ValidationError> validationErrors;
}