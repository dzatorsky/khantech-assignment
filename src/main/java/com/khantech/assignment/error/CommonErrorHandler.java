package com.khantech.assignment.error;

import com.khantech.assignment.error.dto.CommonErrorResponse;
import com.khantech.assignment.error.dto.ValidationError;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class CommonErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<CommonErrorResponse> handleCommonException(CommonException e) {

        logger.error("Common error occurred: ", e);

        CommonErrorResponse commonError = new CommonErrorResponse()
                .setCode(e.getCode())
                .setMessage(e.getMessage())
                .setStatus(e.getStatus().value())
                .setTime(e.getTime() == null ? Instant.now() : e.getTime());

        return ResponseEntity
                .status(e.getStatus())
                .body(commonError);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<CommonErrorResponse> handleUnknownException(Throwable e) {

        logger.error("Unknown error occurred: ", e);

        CommonErrorResponse unknownError = new CommonErrorResponse()
                .setCode("common.error.unknown")
                .setMessage("Unknown error. Please try again later.")
                .setTime(Instant.now())
                .setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(unknownError);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        logger.debug("Validation error occurred: ", ex);

        List<ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> {
                    ValidationError validationError = new ValidationError();
                    validationError.setField(fieldError.getField());
                    validationError.setMessage(fieldError.getDefaultMessage());
                    validationError.setRejectedValue(fieldError.getRejectedValue());
                    return validationError;
                })
                .toList();

        String invalidFieldNames = validationErrors.stream()
                .map(ValidationError::getField)
                .collect(Collectors.joining(", "));

        CommonErrorResponse errorResponse = new CommonErrorResponse()
                .setCode("common.error.invalid-argument")
                .setMessage("The following fields are not valid: " + invalidFieldNames)
                .setStatus(status.value())
                .setTime(Instant.now())
                .setValidationErrors(validationErrors);

        return ResponseEntity
                .status(status)
                .headers(headers)
                .body(errorResponse);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        logger.debug("URL not found: ", ex);

        CommonErrorResponse errorResponse = new CommonErrorResponse()
                .setCode("common.error.not-found")
                .setMessage(ex.getMessage())
                .setTime(Instant.now())
                .setStatus(status.value());

        return ResponseEntity
                .status(status)
                .headers(headers)
                .body(errorResponse);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());

        if (status.is5xxServerError()) {
            ResponseEntity<CommonErrorResponse> resp = handleUnknownException(ex);
            return new ResponseEntity<>(resp.getBody(), resp.getHeaders(), resp.getStatusCode());
        } else {

            logger.debug("Internal spring boot exception happened: ", ex);

            CommonErrorResponse errorResponse = new CommonErrorResponse()
                    .setCode(getErrorCode(statusCode))
                    .setMessage(ex.getMessage())
                    .setTime(Instant.now())
                    .setStatus(status.value());

            return ResponseEntity
                    .status(statusCode)
                    .headers(headers)
                    .body(errorResponse);
        }

    }

    public String getErrorCode(HttpStatusCode statusCode) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());

        String prefix = "common.error.";

        return prefix + status.name()
                .toLowerCase()
                .replaceAll("_", "-");
    }

}
