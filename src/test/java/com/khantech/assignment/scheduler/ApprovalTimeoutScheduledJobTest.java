package com.khantech.assignment.scheduler;

import com.khantech.assignment.TestContainerConfiguration;
import com.khantech.assignment.config.AppProperties;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(TestContainerConfiguration.class)
class ApprovalTimeoutScheduledJobTest {

    public static final String TEST_CURRENCY = "USD";
    public static final String TEST_USER_NAME = "Test User";
    public static final Duration TIMEOUT_DURATION = Duration.ofDays(1);
    public static final Instant EXPIRATION_TIME = Instant.now().minus(TIMEOUT_DURATION);
    public static final BigDecimal TEST_BALANCE = BigDecimal.valueOf(1000L);
    public static final int BATCH_SIZE = 5;

    @MockBean(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
    private AppProperties appProperties;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ApprovalTimeoutScheduledJob scheduledJob;

    private UserEntity testUser;
    private WalletEntity testWallet;

    @BeforeEach
    void setup() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new UserEntity();
        testUser.setName(TEST_USER_NAME);
        userRepository.save(testUser);

        testWallet = new WalletEntity();
        testWallet.setUser(testUser);
        testWallet.setCurrency(TEST_CURRENCY);
        testWallet.setBalance(TEST_BALANCE);
        walletRepository.save(testWallet);

        when(appProperties.getWallet().getTransaction().getApprovalTimeout()).thenReturn(TIMEOUT_DURATION);
        when(appProperties.getScheduler().getApprovalTimeoutJob().getBatchSize()).thenReturn(BATCH_SIZE);
    }

    @Test
    @DisplayName("Should reject transactions with AWAITING_APPROVAL status which have expired")
    void shouldRejectExpiredTransactions() {
        TransactionEntity expiredTransaction = saveTransaction(TransactionStatus.AWAITING_APPROVAL, EXPIRATION_TIME);

        scheduledJob.approveExpiredTransactions();

        // Check that status is REJECTED
        TransactionEntity transactionInDb = transactionRepository.findById(expiredTransaction.getId()).orElseThrow();
        assertThat(transactionInDb.getStatus()).isEqualTo(TransactionStatus.REJECTED);

        // Check that wallet balance unchanged
        WalletEntity walletInDb = walletRepository.findById(testWallet.getId()).orElseThrow();
        assertThat(walletInDb.getBalance()).isEqualByComparingTo(TEST_BALANCE);
    }

    @Test
    @DisplayName("Should reject all transactions in batch with AWAITING_APPROVAL status when batch size is the same as number of transactions")
    void shouldRejectExpiredTransactionsInBatchWhenBatchSameAsNumberOfTransactions() {
        int batchSize = 3;
        when(appProperties.getScheduler().getApprovalTimeoutJob().getBatchSize()).thenReturn(batchSize);

        // Save 3 multiple expired transactions
        saveTransaction(TransactionStatus.AWAITING_APPROVAL, EXPIRATION_TIME);
        saveTransaction(TransactionStatus.AWAITING_APPROVAL, EXPIRATION_TIME);
        saveTransaction(TransactionStatus.AWAITING_APPROVAL, EXPIRATION_TIME);

        scheduledJob.approveExpiredTransactions();

        // Fetch all transactions and check their statuses
        Iterable<TransactionEntity> transactions = transactionRepository.findAll();
        assertThat(transactions).hasSize(3);

        transactions.forEach(transaction -> {
            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.REJECTED);
        });

        // Check that wallet balance is unchanged
        WalletEntity walletInDb = walletRepository.findById(testWallet.getId()).orElseThrow();
        assertThat(walletInDb.getBalance()).isEqualByComparingTo(TEST_BALANCE);
    }

    @Test
    @DisplayName("Should reject only 3 transactions our of 5 in batch with AWAITING_APPROVAL status because batch size is 3")
    void shouldRejectExpiredTransactionsInBatchWhenBatchSmallerThanNumberOfTransactions() {
        int batchSize = 3;
        when(appProperties.getScheduler().getApprovalTimeoutJob().getBatchSize()).thenReturn(batchSize);

        // Save 3 multiple expired transactions
        saveTransaction(TransactionStatus.AWAITING_APPROVAL, EXPIRATION_TIME);
        saveTransaction(TransactionStatus.AWAITING_APPROVAL, EXPIRATION_TIME);
        saveTransaction(TransactionStatus.AWAITING_APPROVAL, EXPIRATION_TIME);

        // Additional 2 transactions outside of the batch
        saveTransaction(TransactionStatus.AWAITING_APPROVAL, EXPIRATION_TIME);
        saveTransaction(TransactionStatus.AWAITING_APPROVAL, EXPIRATION_TIME);

        scheduledJob.approveExpiredTransactions();

        long rejectedAmount = transactionRepository.findAll()
                .stream()
                .filter(transaction -> transaction.getStatus() == TransactionStatus.REJECTED)
                .count();
        assertThat(rejectedAmount).isEqualTo(3);

        long stillWaitingAmount = transactionRepository.findAll()
                .stream()
                .filter(transaction -> transaction.getStatus() == TransactionStatus.AWAITING_APPROVAL)
                .count();
        assertThat(stillWaitingAmount).isEqualTo(2);

        // Check that wallet balance is unchanged
        WalletEntity walletInDb = walletRepository.findById(testWallet.getId()).orElseThrow();
        assertThat(walletInDb.getBalance()).isEqualByComparingTo(TEST_BALANCE);
    }

    @Test
    @DisplayName("Should not reject transactions with AWAITING_APPROVAL status which have not expired yet")
    void shouldNotRejectNonExpiredTransactions() {
        TransactionEntity nonExpiredTransaction = saveTransaction(TransactionStatus.AWAITING_APPROVAL, EXPIRATION_TIME.plusSeconds(30));

        scheduledJob.approveExpiredTransactions();

        TransactionEntity transactionInDb = transactionRepository.findById(nonExpiredTransaction.getId()).orElseThrow();
        assertThat(transactionInDb.getStatus()).isEqualTo(TransactionStatus.AWAITING_APPROVAL);
    }

    @ParameterizedTest
    @EnumSource(value = TransactionStatus.class, names = {"AWAITING_APPROVAL"}, mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Should not reject expired transactions with other statuses")
    void shouldNotRejectTransactionsWithOtherStatuses(TransactionStatus status) {
        TransactionEntity nonExpiredTransaction = saveTransaction(status, EXPIRATION_TIME);

        scheduledJob.approveExpiredTransactions();

        TransactionEntity transactionInDb = transactionRepository.findById(nonExpiredTransaction.getId()).orElseThrow();
        assertThat(transactionInDb.getStatus()).isEqualTo(status);
    }

    private TransactionEntity saveTransaction(TransactionStatus status, Instant now) {
        TransactionEntity testTransaction = new TransactionEntity()
                .setRequestId(UUID.randomUUID()) // Not important for this test
                .setUser(testUser)
                .setWallet(testWallet)
                .setAmount(BigDecimal.valueOf(1000))
                .setType(TransactionType.DEBIT)
                .setStatus(status)
                .setBalanceBefore(BigDecimal.ZERO)
                .setBalanceAfter(BigDecimal.valueOf(1000))
                .setCreatedAt(now);

        return transactionRepository.save(testTransaction);
    }


}