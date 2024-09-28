package com.khantech.assignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khantech.assignment.dto.CreateWalletDTO;
import com.khantech.assignment.dto.WalletDTO;
import com.khantech.assignment.entity.UserEntity;
import com.khantech.assignment.entity.WalletEntity;
import com.khantech.assignment.repository.TransactionRepository;
import com.khantech.assignment.repository.UserRepository;
import com.khantech.assignment.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WalletControllerTest {

    public static final String TEST_CURRENCY = "USD";
    public static final String TEST_USER_NAME = "Test User";

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

    private UserEntity testUser;

    @BeforeEach
    void setup() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();

        this.testUser = new UserEntity();
        this.testUser.setName(TEST_USER_NAME);
        this.testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should create wallet when all conditions are met")
    void testCreateWalletSuccess() throws Exception {
        CreateWalletDTO createDTO = new CreateWalletDTO();
        createDTO.setUserId(testUser.getId());
        createDTO.setCurrency(TEST_CURRENCY);
        String jsonResp = mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        assertThat(walletRepository.findAll()).hasSize(1);

        WalletDTO walletDTO = objectMapper.readValue(jsonResp, WalletDTO.class);
        assertThat(walletDTO.getId()).isNotNull();
        assertThat(walletDTO.getUserId()).isEqualByComparingTo(testUser.getId());
        assertThat(walletDTO.getCurrency()).isEqualTo(TEST_CURRENCY);
        assertThat(walletDTO.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);

        WalletEntity walletInDB = walletRepository.findAll().getFirst();
        assertThat(walletInDB.getUser().getId()).isEqualByComparingTo(testUser.getId());
        assertThat(walletInDB.getCurrency()).isEqualTo(TEST_CURRENCY);
        assertThat(walletInDB.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should not create wallet when the user doesn't exist")
    void testCreateWalletUserNotFound() throws Exception {
        CreateWalletDTO createWalletDTO = new CreateWalletDTO();
        createWalletDTO.setUserId(UUID.randomUUID()); // Non-existent user ID
        createWalletDTO.setCurrency(TEST_CURRENCY);

        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createWalletDTO)))
                .andExpect(status().isNotFound());

        assertThat(walletRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Should not create a wallet if it already exists for a given userId and currency combination")
    void testCreateWalletWalletAlreadyExists() throws Exception {
        WalletEntity existingWallet = new WalletEntity();
        existingWallet.setUser(testUser);
        existingWallet.setCurrency(TEST_CURRENCY);
        existingWallet.setBalance(BigDecimal.valueOf(10000L));
        walletRepository.save(existingWallet);

        CreateWalletDTO createWalletDTO = new CreateWalletDTO();
        createWalletDTO.setUserId(testUser.getId());
        createWalletDTO.setCurrency(TEST_CURRENCY);

        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createWalletDTO)))
                .andExpect(status().isConflict());

    }

    @Test
    @DisplayName("Should not create wallet when request validation fails")
    void testCreateWalletValidation() throws Exception {
        CreateWalletDTO createWalletDTO = new CreateWalletDTO();

        // currency is blank
        createWalletDTO.setUserId(testUser.getId());
        createWalletDTO.setCurrency("");
        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createWalletDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.validationErrors", hasSize(1)))
                .andExpect(jsonPath("$.validationErrors[0].field").value("currency"))
                .andExpect(jsonPath("$.validationErrors[0].message").value("must not be blank"))
                .andExpect(jsonPath("$.validationErrors[0].rejectedValue").value(""));

        // Null userId
        createWalletDTO.setCurrency("USD");
        createWalletDTO.setUserId(null);
        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createWalletDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.validationErrors", hasSize(1)))
                .andExpect(jsonPath("$.validationErrors[0].field").value("userId"))
                .andExpect(jsonPath("$.validationErrors[0].message").value("must not be null"))
                .andExpect(jsonPath("$.validationErrors[0].rejectedValue").isEmpty());

        assertThat(walletRepository.findAll()).isEmpty();
    }

}