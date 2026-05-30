package com.financecontrol.service;

import com.financecontrol.entity.User;
import com.financecontrol.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailSchedulerTest {

    @Mock UserRepository userRepository;
    @Mock EmailService   emailService;

    // EmailScheduler uses a constructor with @Value; build it manually in each test.

    // ── sendWeeklyReminders – com usuários ───────────────────────────────────

    @Test
    void sendWeeklyReminders_comUsuariosNodia_enviEmails() {
        EmailScheduler scheduler = new EmailScheduler(userRepository, emailService, "UTC");

        User user = userWith(1L, "joao", "joao@test.com");
        int today = LocalDate.now(ZoneId.of("UTC")).getDayOfWeek().getValue();
        when(userRepository.findByEmailNotificationEnabledTrueAndEmailNotificationDay(today))
                .thenReturn(List.of(user));

        scheduler.sendWeeklyReminders();

        verify(emailService).sendWeeklyReminder(user);
    }

    @Test
    void sendWeeklyReminders_semUsuariosNodia_naoEnviaEmail() {
        EmailScheduler scheduler = new EmailScheduler(userRepository, emailService, "UTC");

        int today = LocalDate.now(ZoneId.of("UTC")).getDayOfWeek().getValue();
        when(userRepository.findByEmailNotificationEnabledTrueAndEmailNotificationDay(today))
                .thenReturn(List.of());

        scheduler.sendWeeklyReminders();

        verify(emailService, never()).sendWeeklyReminder(any());
    }

    @Test
    void sendWeeklyReminders_variosUsuarios_enviaTodosEmails() {
        EmailScheduler scheduler = new EmailScheduler(userRepository, emailService, "America/Sao_Paulo");

        int today = LocalDate.now(ZoneId.of("America/Sao_Paulo")).getDayOfWeek().getValue();
        User u1 = userWith(1L, "u1", "u1@test.com");
        User u2 = userWith(2L, "u2", "u2@test.com");
        when(userRepository.findByEmailNotificationEnabledTrueAndEmailNotificationDay(today))
                .thenReturn(List.of(u1, u2));

        scheduler.sendWeeklyReminders();

        verify(emailService).sendWeeklyReminder(u1);
        verify(emailService).sendWeeklyReminder(u2);
    }

    // ── helper ───────────────────────────────────────────────────────────────

    private static User userWith(Long id, String username, String email) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(email);
        u.setEmailNotificationEnabled(true);
        u.setEmailNotificationDay(LocalDate.now().getDayOfWeek().getValue());
        return u;
    }
}
