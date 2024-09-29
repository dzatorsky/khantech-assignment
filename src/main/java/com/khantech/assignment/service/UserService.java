package com.khantech.assignment.service;

import com.khantech.assignment.dto.CreateUserRequest;
import com.khantech.assignment.dto.User;
import com.khantech.assignment.entity.UserEntity;
import com.khantech.assignment.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(CreateUserRequest request) {
        UserEntity entity = new UserEntity();
        entity.setName(request.getName());

        userRepository.save(entity);

        User user = new User();
        user.setId(entity.getId());
        user.setName(entity.getName());

        return user;
    }
}
