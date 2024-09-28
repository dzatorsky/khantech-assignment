package com.khantech.assignment.service;

import com.khantech.assignment.dto.CreateUserDTO;
import com.khantech.assignment.dto.UserDTO;
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

    public UserDTO createUser(CreateUserDTO dto) {
        UserEntity entity = new UserEntity();
        entity.setName(dto.getName());

        userRepository.save(entity);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(entity.getId());
        userDTO.setName(entity.getName());

        return userDTO;
    }
}
