package com.khantech.assignment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.time.Duration;

@Data
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
        private BigDecimal threshold;
        private Duration approvalTimeout;
    }

    @Data
    public static class Scheduler {
        private Boolean enabled;
        private ApprovalTimeoutScheduledJob approvalTimeoutJob = new ApprovalTimeoutScheduledJob();
    }

    @Data
    public static class ScheduledJob {
        private String cron;
    }

    @Data
    public static class ApprovalTimeoutScheduledJob extends ScheduledJob {
        private Integer batchSize;
    }
}
