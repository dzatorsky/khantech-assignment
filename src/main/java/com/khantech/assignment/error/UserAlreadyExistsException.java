package com.khantech.assignment.error;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class UserAlreadyExistsException extends CommonException {

    public UserAlreadyExistsException(UUID userId) {
        super("user-already-exists", "User already exists with id " + userId, HttpStatus.CONFLICT);
    }

}
