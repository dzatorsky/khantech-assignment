package com.khantech.assignment.error;

import com.khantech.assignment.error.dto.ValidationError;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class CommonException extends RuntimeException {

    private final String code;

    private final String message;

    private final HttpStatus status;

    private String details;

    private String origin;

    private Instant time;

    private List<ValidationError> validationErrors;

    protected CommonException(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    protected CommonException(String code, HttpStatus status, Throwable cause) {
        super(cause);
        this.code = code;
        this.message = cause.getMessage();
        this.status = status;
        this.details = cause.getCause() != null ? cause.getCause().getMessage() : null;
    }

}