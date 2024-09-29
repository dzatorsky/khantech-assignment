package com.khantech.assignment.scheduler;

import com.khantech.assignment.config.AppProperties;
import com.khantech.assignment.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalTimeoutScheduledJob {

    private final WalletService walletService;
    private final AppProperties appProperties;

    @Transactional
    @Scheduled(cron = "${app.scheduler.approval-timeout-job.cron}")
    public void approveExpiredTransactions() {
        log.info("Scheduled Job for approving expired transactions started.");
        Integer batchSize = appProperties.getScheduler().getApprovalTimeoutJob().getBatchSize();
        walletService.rejectExpiredTransactions(batchSize);
        log.info("Scheduled Job for approving expired transactions finished.");
    }
}
