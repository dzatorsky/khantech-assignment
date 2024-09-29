package com.khantech.assignment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Wallet wallet = new Wallet();

    @Data
    public static class Wallet {
        private Transaction transaction = new Transaction();
    }

    @Data
    public static class Transaction {
        private BigDecimal threshold = BigDecimal.ZERO;
    }
}
