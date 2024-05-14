package edu.example.springmvcdemo.config;

import edu.example.springmvcdemo.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
@RequiredArgsConstructor
public class SchedulingConfig {

    private final RefreshTokenService tokenService;

    @Scheduled(cron = "${scheduler.deleteExpiredTokensCron}")
    public void deleteExpiredRefreshTokens() {
        tokenService.deleteExpiredRefreshTokens();
    }
}
