package com.khantech.assignment.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khantech.assignment.TestContainerConfiguration;
import com.khantech.assignment.dto.CreateUserRequest;
import com.khantech.assignment.dto.User;
import com.khantech.assignment.entity.UserEntity;
import com.khantech.assignment.repository.TransactionRepository;
import com.khantech.assignment.repository.UserRepository;
import com.khantech.assignment.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainerConfiguration.class)
class UserControllerTest {

    public static final String TEST_USER_NAME = "John Doe";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setup() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create user successfully when all conditions are met")
    void testCreateUserSuccess() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setName(TEST_USER_NAME);

        String jsonResp = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        User createdUser = objectMapper.readValue(jsonResp, User.class);
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getName()).isEqualTo(request.getName());

        assertThat(userRepository.findAll()).hasSize(1);
        UserEntity userInDB = userRepository.findById(createdUser.getId()).orElseThrow();
        assertThat(userInDB.getName()).isEqualTo(request.getName());
    }

    @Test
    @DisplayName("Should not create user when name is blank")
    void testCreateUserNameBlank() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setName(""); // Blank name

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.validationErrors", hasSize(1)))
                .andExpect(jsonPath("$.validationErrors[0].field").value("name"))
                .andExpect(jsonPath("$.validationErrors[0].message").value("must not be blank"))
                .andExpect(jsonPath("$.validationErrors[0].rejectedValue").value(""));

        assertThat(userRepository.findAll()).isEmpty();
    }
}