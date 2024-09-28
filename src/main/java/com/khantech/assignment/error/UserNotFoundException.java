package com.khantech.assignment.error;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class UserNotFoundException extends CommonException {

    public UserNotFoundException(UUID userId) {
        super("user-not-found", "User with id " + userId + " could not be found", HttpStatus.NOT_FOUND);
    }

}
