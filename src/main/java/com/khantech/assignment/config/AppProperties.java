package com.khantech.assignment.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.Duration;

@Data
@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Wallet wallet = new Wallet();
    private Scheduler scheduler = new Scheduler();

    @Data
    public static class Wallet {
        private Transaction transaction = new Transaction();
    }

    @Data
    public static class Transaction {
        @NotNull
        private BigDecimal threshold;

        @NotNull
        private Duration approvalTimeout;
    }

    @Data
    public static class Scheduler {
        @NotNull
        private Boolean enabled;
        private ApprovalTimeoutScheduledJob approvalTimeoutJob = new ApprovalTimeoutScheduledJob();
    }

    @Data
    public static class ScheduledJob {
        @NotNull
        private String cron;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ApprovalTimeoutScheduledJob extends ScheduledJob {
        @NotNull
        private Integer batchSize;
    }
}
