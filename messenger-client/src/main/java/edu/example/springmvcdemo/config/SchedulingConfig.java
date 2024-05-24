package edu.example.springmvcdemo.config;

import edu.example.springmvcdemo.service.MessageService;
import edu.example.springmvcdemo.service.RoomService;
import edu.example.springmvcdemo.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import static java.util.Objects.isNull;

@Configuration
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class SchedulingConfig {

    private final UserSessionService userSessionService;
    private final RoomService roomService;
    private final MessageService messageService;

    @Scheduled(fixedRate = 60_000, initialDelay = 0)
    public void updateTokens() {
        userSessionService.updateTokens();
        log.info("Tokens update success");
    }

    @Scheduled(cron = "*/5 * * * * *")
    public void updateLocalData() {
        if (isNull(userSessionService.getAccessToken())) {
            return;
        }
        roomService.updateRooms();
        messageService.pullMessages();
        log.info("Update local data success");
    }
}
