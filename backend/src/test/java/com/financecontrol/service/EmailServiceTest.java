package com.financecontrol.service;

import com.financecontrol.entity.Goal;
import com.financecontrol.entity.User;
import com.financecontrol.entity.UserFeedback;
import com.financecontrol.enums.FeedbackType;
import com.financecontrol.enums.GoalNotificationType;
import com.financecontrol.enums.GoalStatus;
import com.financecontrol.enums.GoalType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.internet.MimeMessage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailServiceTest {

    @Mock JavaMailSender mailSender;
    @Mock MessageSource messageSource;

    EmailService service;

    @BeforeEach
    void setUp() {
        service = new EmailService(mailSender, messageSource, "http://localhost:8080", "test@test.com");
        when(messageSource.getMessage(any(String.class), any(), any(Locale.class))).thenReturn("msg");
        when(mailSender.createMimeMessage())
                .thenReturn(new MimeMessage((jakarta.mail.Session) null));
    }

    private static User userWith(String language) {
        User u = new User();
        u.setUsername("Artur");
        u.setEmail("artur@test.com");
        u.setLanguage(language);
        return u;
    }

    private static Goal sampleGoal(GoalType type) {
        Goal g = new Goal();
        g.setName("Meta Teste");
        g.setType(type);
        g.setTargetAmount(5000.0);
        g.setStartDate(LocalDate.now().withDayOfMonth(1));
        g.setEndDate(LocalDate.now().plusMonths(3));
        g.setStatus(GoalStatus.ACTIVE);
        g.setCreatedAt(LocalDateTime.now());
        return g;
    }

    private static UserFeedback feedbackWith(FeedbackType type, Integer nps) {
        UserFeedback f = new UserFeedback();
        f.setType(type);
        f.setMessage("Mensagem de feedback");
        f.setNpsScore(nps);
        f.setUser(userWith("pt"));
        return f;
    }

    @Test
    void sendVerificationEmail_enviaMime() {
        service.sendVerificationEmail(userWith("pt"), "token123");
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendVerificationEmail_englishLocale() {
        service.sendVerificationEmail(userWith("en"), "token123");
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendWeeklyReminder_enviaMime() {
        service.sendWeeklyReminder(userWith("es"));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendWeeklyReminder_languageNull_naoLancaExcecao() {
        assertThatCode(() -> service.sendWeeklyReminder(userWith(null))).doesNotThrowAnyException();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendTestEmail_enviaMime() throws Exception {
        service.sendTestEmail(userWith("pt"));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @ParameterizedTest
    @EnumSource(GoalNotificationType.class)
    void sendTestGoalEmail_todosOsTipos(GoalNotificationType type) throws Exception {
        service.sendTestGoalEmail(userWith("pt"), type);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @ParameterizedTest
    @EnumSource(GoalNotificationType.class)
    void sendGoalNotification_todosOsTipos_savings(GoalNotificationType type) {
        service.sendGoalNotification(userWith("pt"), sampleGoal(GoalType.SAVINGS), type, 2500.0);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendGoalNotification_expenseLimit_progressColors() {
        Goal g = sampleGoal(GoalType.EXPENSE_LIMIT);
        service.sendGoalNotification(userWith("pt"), g, GoalNotificationType.MILESTONE_90, 4600.0);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendGoalNotification_targetZero_pctZero() {
        Goal g = sampleGoal(GoalType.SAVINGS);
        g.setTargetAmount(0.0);
        service.sendGoalNotification(userWith("pt"), g, GoalNotificationType.MILESTONE_50, 100.0);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @ParameterizedTest
    @EnumSource(FeedbackType.class)
    void sendFeedbackNotification_todosOsTipos(FeedbackType type) {
        User admin = userWith("pt");
        admin.setEmail("admin@test.com");
        service.sendFeedbackNotification(admin, userWith("pt"), feedbackWith(type, 8));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendFeedbackNotification_npsNull_naoLancaExcecao() {
        User admin = userWith("pt");
        assertThatCode(() -> service.sendFeedbackNotification(admin, userWith("pt"),
                feedbackWith(FeedbackType.GENERAL, null))).doesNotThrowAnyException();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void asyncMethod_capturaExcecaoInternamente() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("boom"));
        assertThatCode(() -> service.sendVerificationEmail(userWith("pt"), "t")).doesNotThrowAnyException();
        assertThatCode(() -> service.sendWeeklyReminder(userWith("pt"))).doesNotThrowAnyException();
        assertThatCode(() -> service.sendGoalNotification(userWith("pt"),
                sampleGoal(GoalType.SAVINGS), GoalNotificationType.COMPLETED, 5000.0)).doesNotThrowAnyException();
    }
}
