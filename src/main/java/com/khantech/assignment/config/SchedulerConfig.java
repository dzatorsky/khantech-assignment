package com.khantech.assignment.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This config is needed so we could disable scheduling in tests which could introduce unpredictable behaviour
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerConfig {

}