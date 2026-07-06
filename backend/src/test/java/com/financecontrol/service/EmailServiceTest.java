package com.financecontrol.service;

import com.financecontrol.entity.User;
import com.financecontrol.entity.UserFeedback;
import com.financecontrol.enums.FeedbackType;
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

import java.util.Locale;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailServiceTest {

    @Mock BrevoMailClient brevoClient;
    @Mock MessageSource messageSource;

    EmailService service;

    @BeforeEach
    void setUp() {
        service = new EmailService(brevoClient, messageSource, "http://localhost:8080", "test@test.com", "Finance Control");
        when(messageSource.getMessage(any(String.class), any(), any(Locale.class))).thenReturn("msg");
    }

    private static User userWith(String language) {
        User u = new User();
        u.setUsername("Artur");
        u.setEmail("artur@test.com");
        u.setLanguage(language);
        return u;
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
    void sendVerificationEmail_enviaMime() throws Exception {
        service.sendVerificationEmail(userWith("pt"), "token123");
        verify(brevoClient).send(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void sendVerificationEmail_englishLocale() throws Exception {
        service.sendVerificationEmail(userWith("en"), "token123");
        verify(brevoClient).send(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void sendWeeklyReminder_enviaMime() throws Exception {
        service.sendWeeklyReminder(userWith("es"));
        verify(brevoClient).send(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void sendWeeklyReminder_languageNull_naoLancaExcecao() throws Exception {
        assertThatCode(() -> service.sendWeeklyReminder(userWith(null))).doesNotThrowAnyException();
        verify(brevoClient).send(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void sendTestEmail_enviaMime() throws Exception {
        service.sendTestEmail(userWith("pt"));
        verify(brevoClient).send(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void sendTestGoalDeadlineEmail_enviaMime() throws Exception {
        service.sendTestGoalDeadlineEmail(userWith("pt"));
        verify(brevoClient).send(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @ParameterizedTest
    @EnumSource(FeedbackType.class)
    void sendFeedbackNotification_todosOsTipos(FeedbackType type) throws Exception {
        User admin = userWith("pt");
        admin.setEmail("admin@test.com");
        service.sendFeedbackNotification(admin, userWith("pt"), feedbackWith(type, 8));
        verify(brevoClient).send(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void sendFeedbackNotification_npsNull_naoLancaExcecao() throws Exception {
        User admin = userWith("pt");
        assertThatCode(() -> service.sendFeedbackNotification(admin, userWith("pt"),
                feedbackWith(FeedbackType.GENERAL, null))).doesNotThrowAnyException();
        verify(brevoClient).send(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void asyncMethod_capturaExcecaoInternamente() throws Exception {
        doThrow(new jakarta.mail.MessagingException("boom"))
                .when(brevoClient).send(anyString(), anyString(), anyString(), anyString(), anyString());
        assertThatCode(() -> service.sendVerificationEmail(userWith("pt"), "t")).doesNotThrowAnyException();
        assertThatCode(() -> service.sendWeeklyReminder(userWith("pt"))).doesNotThrowAnyException();
    }
}
