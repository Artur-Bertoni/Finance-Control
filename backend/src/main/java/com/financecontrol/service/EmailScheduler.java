package com.financecontrol.service;

import com.financecontrol.entity.User;
import com.financecontrol.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailScheduler {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendWeeklyReminders() {
        int today = LocalDate.now().getDayOfWeek().getValue();
        List<User> users = userRepository.findByEmailNotificationEnabledTrueAndEmailNotificationDay(today);

        if (users.isEmpty()) return;

        log.info("Enviando emails semanais para {} usuário(s) (dia={})", users.size(), today);
        
        users.forEach(emailService::sendWeeklyReminder);
    }
}
