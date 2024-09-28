package com.khantech.assignment.controller;

import com.khantech.assignment.dto.CreateUserDTO;
import com.khantech.assignment.dto.UserDTO;
import com.khantech.assignment.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserDTO dto) {
        UserDTO createdUser = userService.createUser(dto);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

}