package com.khantech.assignment.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khantech.assignment.TestContainerConfiguration;
import com.khantech.assignment.config.AppProperties;
import com.khantech.assignment.dto.CreateWalletRequest;
import com.khantech.assignment.dto.SubmitTransactionRequest;
import com.khantech.assignment.dto.Transaction;
import com.khantech.assignment.dto.Wallet;
import com.khantech.assignment.entity.TransactionEntity;
import com.khantech.assignment.entity.UserEntity;
import com.khantech.assignment.entity.WalletEntity;
import com.khantech.assignment.enums.TransactionStatus;
import com.khantech.assignment.enums.TransactionType;
import com.khantech.assignment.repository.TransactionRepository;
import com.khantech.assignment.repository.UserRepository;
import com.khantech.assignment.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainerConfiguration.class)
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

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private AppProperties appProperties;

    private UserEntity testUser;

    @BeforeEach
    void setup() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new UserEntity();
        testUser.setName(TEST_USER_NAME);

        userRepository.save(testUser);
    }

    @Nested
    @DisplayName("API endpoint for wallet creation")
    class TestWalletCreation {

        @Test
        @DisplayName("Should create wallet when all conditions are met")
        void testCreateWalletSuccess() throws Exception {
            CreateWalletRequest request = new CreateWalletRequest();
            request.setUserId(testUser.getId());
            request.setCurrency(TEST_CURRENCY);

            String jsonResp = mockMvc.perform(post("/api/wallets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn().getResponse().getContentAsString();

            assertThat(walletRepository.findAll()).hasSize(1);

            Wallet wallet = objectMapper.readValue(jsonResp, Wallet.class);
            assertThat(wallet.getId()).isNotNull();
            assertThat(wallet.getUserId()).isEqualByComparingTo(testUser.getId());
            assertThat(wallet.getCurrency()).isEqualTo(TEST_CURRENCY);
            assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);

            WalletEntity walletInDB = walletRepository.findAll().getFirst();
            assertThat(walletInDB.getUser().getId()).isEqualByComparingTo(testUser.getId());
            assertThat(walletInDB.getCurrency()).isEqualTo(TEST_CURRENCY);
            assertThat(walletInDB.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should not create wallet when the user doesn't exist")
        void testCreateWalletUserNotFound() throws Exception {
            CreateWalletRequest request = new CreateWalletRequest();
            request.setUserId(UUID.randomUUID()); // Non-existent user ID
            request.setCurrency(TEST_CURRENCY);

            mockMvc.perform(post("/api/wallets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
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

            CreateWalletRequest request = new CreateWalletRequest();
            request.setUserId(testUser.getId());
            request.setCurrency(TEST_CURRENCY);

            mockMvc.perform(post("/api/wallets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should not create wallet when request validation fails")
        void testCreateWalletValidation() throws Exception {
            CreateWalletRequest request = new CreateWalletRequest();

            // Currency is blank
            request.setCurrency("");
            request.setUserId(testUser.getId());
            mockMvc.perform(post("/api/wallets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.validationErrors", hasSize(1)))
                    .andExpect(jsonPath("$.validationErrors[0].field").value("currency"))
                    .andExpect(jsonPath("$.validationErrors[0].message").value("must not be blank"))
                    .andExpect(jsonPath("$.validationErrors[0].rejectedValue").value(""));

            // Null userId
            request.setUserId(null);
            request.setCurrency("USD");
            mockMvc.perform(post("/api/wallets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.validationErrors", hasSize(1)))
                    .andExpect(jsonPath("$.validationErrors[0].field").value("userId"))
                    .andExpect(jsonPath("$.validationErrors[0].message").value("must not be null"))
                    .andExpect(jsonPath("$.validationErrors[0].rejectedValue").isEmpty());

            assertThat(walletRepository.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("API endpoint for transaction submission")
    class TestTransactionSubmission {
        public static final BigDecimal TEST_BALANCE = BigDecimal.valueOf(100_000);
        public static final BigDecimal THRESHOLD = BigDecimal.valueOf(5000);
        public static final BigDecimal AMOUNT_WITHIN_THRESHOLD = BigDecimal.valueOf(200);
        public static final BigDecimal AMOUNT_ABOVE_THRESHOLD = THRESHOLD.add(BigDecimal.ONE);

        private WalletEntity testWallet;

        @BeforeEach
        void setup() {
            walletRepository.deleteAll();

            testWallet = new WalletEntity();
            testWallet.setUser(testUser);
            testWallet.setCurrency(TEST_CURRENCY);
            testWallet.setBalance(TEST_BALANCE);
            testWallet = walletRepository.save(testWallet);

            when(appProperties.getWallet().getTransaction().getThreshold()).thenReturn(THRESHOLD);
        }

        @Test
        @DisplayName("Should save transaction in DB with all the fields mapped properly without null values")
        void testSubmitCreditTransaction() throws Exception {
            SubmitTransactionRequest request = new SubmitTransactionRequest()
                    .setAmount(AMOUNT_WITHIN_THRESHOLD)
                    .setType(TransactionType.CREDIT);

            Transaction response = submitTransaction(request);

            assertThat(response.getId()).isNotNull();
            assertThat(response.getUserId()).isEqualByComparingTo(testUser.getId());
            assertThat(response.getWalletId()).isEqualByComparingTo(testWallet.getId());
            assertThat(response.getAmount()).isEqualByComparingTo(AMOUNT_WITHIN_THRESHOLD);
            assertThat(response.getType()).isEqualTo(TransactionType.CREDIT);
            assertThat(response.getBalanceBefore()).isEqualByComparingTo(TEST_BALANCE);
            assertThat(response.getBalanceAfter()).isEqualByComparingTo(TEST_BALANCE.add(AMOUNT_WITHIN_THRESHOLD));
            assertThat(response.getStatus()).isEqualTo(TransactionStatus.APPROVED);
            assertThat(response.getCreatedAt()).isNotNull();

            assertThat(response).hasNoNullFieldsOrProperties();

            assertThat(transactionRepository.findAll()).hasSize(1);
            TransactionEntity savedTransaction = transactionRepository.findAll().getFirst();
            assertThat(savedTransaction.getId()).isNotNull();
            assertThat(savedTransaction.getUser().getId()).isEqualByComparingTo(testUser.getId());
            assertThat(savedTransaction.getAmount()).isEqualByComparingTo(AMOUNT_WITHIN_THRESHOLD);
            assertThat(savedTransaction.getType()).isEqualTo(TransactionType.CREDIT);
            assertThat(savedTransaction.getBalanceBefore()).isEqualByComparingTo(TEST_BALANCE);
            assertThat(savedTransaction.getBalanceAfter()).isEqualByComparingTo(TEST_BALANCE.add(AMOUNT_WITHIN_THRESHOLD));
            assertThat(savedTransaction.getStatus()).isEqualTo(TransactionStatus.APPROVED);
            assertThat(savedTransaction.getCreatedAt()).isNotNull();

            assertThat(savedTransaction).hasNoNullFieldsOrProperties();
        }

        @Test
        @DisplayName("Should automatically add money to the user wallet for transactions under the threshold")
        void testCreditTransaction() throws Exception {
            SubmitTransactionRequest request = new SubmitTransactionRequest()
                    .setAmount(AMOUNT_WITHIN_THRESHOLD)
                    .setType(TransactionType.CREDIT);

            BigDecimal expectedBalance = TEST_BALANCE.add(AMOUNT_WITHIN_THRESHOLD);

            Transaction response = submitTransaction(request);
            verifyBalanceChange(response, expectedBalance);

            TransactionEntity transaction = transactionRepository.findById(response.getId()).orElseThrow();
            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.APPROVED);
        }

        @Test
        @DisplayName("Should automatically deduct money from the user wallet for transactions under the threshold")
        void testDebitTransaction() throws Exception {
            SubmitTransactionRequest request = new SubmitTransactionRequest()
                    .setAmount(AMOUNT_WITHIN_THRESHOLD)
                    .setType(TransactionType.DEBIT);

            BigDecimal expectedBalance = TEST_BALANCE.subtract(AMOUNT_WITHIN_THRESHOLD);

            Transaction response = submitTransaction(request);
            verifyBalanceChange(response, expectedBalance);

            TransactionEntity transaction = transactionRepository.findById(response.getId()).orElseThrow();
            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.APPROVED);
        }

        @Test
        @DisplayName("Should return error when wallet does not exist")
        void testSubmitTransactionWalletNotFound() throws Exception {
            SubmitTransactionRequest request = new SubmitTransactionRequest()
                    .setAmount(AMOUNT_WITHIN_THRESHOLD)
                    .setType(TransactionType.DEBIT);

            mockMvc.perform(post("/api/wallets/{walletId}/transactions", UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            assertThat(transactionRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("Should return error when debiting user wallet with insufficient funds for transactions under the threshold")
        void testSubmitDebitTransactionWhenInsufficientFunds() throws Exception {
            testWallet.setBalance(BigDecimal.ZERO);
            walletRepository.save(testWallet);

            SubmitTransactionRequest request = new SubmitTransactionRequest()
                    .setAmount(AMOUNT_WITHIN_THRESHOLD)
                    .setType(TransactionType.DEBIT);

            mockMvc.perform(post("/api/wallets/{walletId}/transactions", testWallet.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertThat(transactionRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("Should return error when debiting user wallet with insufficient funds for transactions above the threshold")
        void testSubmitDebitTransactionWhenInsufficientFundsAboveThreshold() throws Exception {
            testWallet.setBalance(BigDecimal.ZERO);
            walletRepository.save(testWallet);

            SubmitTransactionRequest request = new SubmitTransactionRequest()
                    .setAmount(AMOUNT_ABOVE_THRESHOLD)
                    .setType(TransactionType.DEBIT);

            mockMvc.perform(post("/api/wallets/{walletId}/transactions", testWallet.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertThat(transactionRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("Should save transaction above threshold in AWAITING_APPROVAL status without changing wallet balance")
        void testSubmitTransactionWithoutBalanceChangeAboveTheThreshold() throws Exception {
            SubmitTransactionRequest request = new SubmitTransactionRequest()
                    .setAmount(AMOUNT_ABOVE_THRESHOLD)
                    .setType(TransactionType.DEBIT);

            Transaction response = submitTransaction(request);

            verifyBalanceChange(response, TEST_BALANCE);

            TransactionEntity transaction = transactionRepository.findById(response.getId()).orElseThrow();
            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.AWAITING_APPROVAL);
        }

        private Transaction submitTransaction(SubmitTransactionRequest request) throws Exception {
            String jsonResp = mockMvc.perform(post("/api/wallets/{walletId}/transactions", testWallet.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn().getResponse().getContentAsString();

            return objectMapper.readValue(jsonResp, Transaction.class);
        }

        private void verifyBalanceChange(Transaction response, BigDecimal expectedBalance) {
            // CHeck if the response contains properly calculated fields
            assertThat(response.getBalanceBefore()).isEqualByComparingTo(TEST_BALANCE);
            assertThat(response.getBalanceAfter()).isEqualByComparingTo(expectedBalance);

            // Check if the transaction amounts were calculated properly
            assertThat(transactionRepository.findAll()).hasSize(1);
            TransactionEntity savedTransaction = transactionRepository.findAll().getFirst();
            assertThat(savedTransaction.getBalanceBefore()).isEqualByComparingTo(TEST_BALANCE);
            assertThat(savedTransaction.getBalanceAfter()).isEqualByComparingTo(expectedBalance);

            // Check if the wallet balance has been updated
            WalletEntity updatedWallet = walletRepository.findById(testWallet.getId()).orElseThrow();
            assertThat(updatedWallet.getBalance()).isEqualByComparingTo(expectedBalance);
        }
    }

}