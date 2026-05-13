package com.financecontrol.service;

import com.financecontrol.entity.User;
import com.financecontrol.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@Slf4j
public class EmailScheduler {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ZoneId schedulerZone;

    public EmailScheduler(UserRepository userRepository,
                          EmailService emailService,
                          @Value("${app.scheduler.timezone:America/Sao_Paulo}") String timezone) {
        this.userRepository = userRepository;
        this.emailService   = emailService;
        this.schedulerZone  = ZoneId.of(timezone);
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "${app.scheduler.timezone:America/Sao_Paulo}")
    public void sendWeeklyReminders() {
        int today = LocalDate.now(schedulerZone).getDayOfWeek().getValue();
        List<User> users = userRepository.findByEmailNotificationEnabledTrueAndEmailNotificationDay(today);

        if (users.isEmpty()) return;

        log.info("Enviando emails semanais para {} usuário(s) (dia={})", users.size(), today);
        users.forEach(emailService::sendWeeklyReminder);
    }
}
