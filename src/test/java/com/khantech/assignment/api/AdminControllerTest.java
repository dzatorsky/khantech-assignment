package com.khantech.assignment.api;

import com.khantech.assignment.TestContainerConfiguration;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainerConfiguration.class)
class AdminControllerTest {

    public static final BigDecimal TEST_BALANCE = BigDecimal.valueOf(1000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private UserEntity testUser;
    private WalletEntity testWallet;

    @BeforeEach
    void setup() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new UserEntity();
        testUser.setName("Test User");
        userRepository.save(testUser);

        testWallet = new WalletEntity();
        testWallet.setUser(testUser);
        testWallet.setCurrency("USD");
        testWallet.setBalance(TEST_BALANCE);
        walletRepository.save(testWallet);
    }

    @Nested
    @DisplayName("API endpoint for transaction approval")
    class TestApproveTransaction {

        public static final BigDecimal TEST_TRANSACTION_AMOUNT = BigDecimal.valueOf(200);

        @Test
        @DisplayName("Should increase wallet balance after approval of credit transaction")
        void approveCreditTransaction() throws Exception {
            testTransactionApproval(TransactionType.CREDIT, TEST_BALANCE.add(TEST_TRANSACTION_AMOUNT));
        }

        @Test
        @DisplayName("Should decrease wallet balance after approval of debit transaction")
        void approveDebitTransaction() throws Exception {
            testTransactionApproval(TransactionType.DEBIT, TEST_BALANCE.subtract(TEST_TRANSACTION_AMOUNT));
        }

        @Test
        @DisplayName("Should return error when transaction not found")
        void approveTransactionNotFound() throws Exception {
            UUID transactionId = UUID.randomUUID();

            mockMvc.perform(post("/api/admin/transactions/{transactionId}/approve", transactionId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @ParameterizedTest
        @EnumSource(value = TransactionStatus.class, names = {"AWAITING_APPROVAL"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Should return error for invalid transaction state")
        void approveTransactionInvalidState(TransactionStatus initialStatus) throws Exception {
            TransactionEntity transaction = getTestTransaction();
            transaction.setStatus(initialStatus);
            transactionRepository.save(transaction);

            mockMvc.perform(post("/api/admin/transactions/{transactionId}/approve", transaction.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(content().string(containsString("invalid-transaction-state")));

            // Transaction in db should not be changed
            TransactionEntity transactionInDB = transactionRepository.findById(transaction.getId()).orElseThrow();
            assertThat(transactionInDB.getStatus()).isEqualTo(initialStatus);
            assertThat(transactionInDB.getBalanceBefore()).isEqualByComparingTo(transaction.getBalanceBefore());
            assertThat(transactionInDB.getBalanceAfter()).isEqualByComparingTo(transaction.getBalanceAfter());

            // Wallet balance should not be changed
            WalletEntity wallet = walletRepository.findById(testWallet.getId()).orElseThrow();
            assertThat(wallet.getBalance()).isEqualByComparingTo(TEST_BALANCE);
        }

        @Test
        @DisplayName("Should return error when approving transaction results in insufficient funds")
        void approveTransactionInsufficientFunds() throws Exception {
            BigDecimal newBalance = BigDecimal.ZERO;

            testWallet.setBalance(newBalance); // Lower balance to cause insufficient funds
            walletRepository.save(testWallet);

            TransactionEntity transaction = getTestTransaction();
            transaction.setType(TransactionType.DEBIT);
            transactionRepository.save(transaction);

            mockMvc.perform(post("/api/admin/transactions/{transactionId}/approve", transaction.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("insufficient-funds")));

            // Transaction in db should not be changed
            TransactionEntity transactionInDB = transactionRepository.findById(transaction.getId()).orElseThrow();
            assertThat(transactionInDB.getStatus()).isEqualTo(transaction.getStatus());
            assertThat(transactionInDB.getBalanceBefore()).isEqualByComparingTo(transaction.getBalanceBefore());
            assertThat(transactionInDB.getBalanceAfter()).isEqualByComparingTo(transaction.getBalanceAfter());

            // Wallet balance should not be changed
            WalletEntity wallet = walletRepository.findById(testWallet.getId()).orElseThrow();
            assertThat(wallet.getBalance()).isEqualByComparingTo(newBalance);
        }

        private void testTransactionApproval(TransactionType transactionType, BigDecimal expectedBalance) throws Exception {
            TransactionEntity transaction = getTestTransaction();
            transaction.setType(transactionType);
            transactionRepository.save(transaction);

            mockMvc.perform(post("/api/admin/transactions/{transactionId}/approve", transaction.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            TransactionEntity updatedTransaction = transactionRepository.findById(transaction.getId()).orElseThrow();
            assertThat(updatedTransaction.getStatus()).isEqualTo(TransactionStatus.APPROVED);
            assertThat(updatedTransaction.getBalanceBefore()).isEqualByComparingTo(testWallet.getBalance());

            assertThat(updatedTransaction.getBalanceAfter()).isEqualByComparingTo(expectedBalance);

            WalletEntity updatedWallet = walletRepository.findById(testWallet.getId()).orElseThrow();
            assertThat(updatedWallet.getBalance()).isEqualByComparingTo(expectedBalance);
        }

        private TransactionEntity getTestTransaction() {
            return new TransactionEntity()
                    .setRequestId(UUID.randomUUID())
                    .setUser(testUser)
                    .setWallet(testWallet)
                    .setAmount(TEST_TRANSACTION_AMOUNT)
                    .setType(TransactionType.DEBIT)
                    .setStatus(TransactionStatus.AWAITING_APPROVAL)
                    .setBalanceBefore(TEST_BALANCE)
                    .setBalanceAfter(TEST_BALANCE);
        }
    }
}