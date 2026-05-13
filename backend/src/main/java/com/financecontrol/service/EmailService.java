package com.financecontrol.service;

import com.financecontrol.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;

import org.springframework.lang.NonNull;

@Service
@Slf4j
public class EmailService {

    private static final String TEMPLATE_PATH = "templates/weekly-reminder.html";

    private final JavaMailSender mailSender;
    private final MessageSource messageSource;
    private final String baseUrl;
    private final String mailFrom;

    public EmailService(JavaMailSender mailSender,
                        MessageSource messageSource,
                        @Value("${app.base-url}") String baseUrl,
                        @Value("${app.mail.from}") String mailFrom) {
        this.mailSender     = mailSender;
        this.messageSource  = messageSource;
        this.baseUrl        = Objects.requireNonNull(baseUrl,  "app.base-url must be configured");
        this.mailFrom       = Objects.requireNonNull(mailFrom, "app.mail.from must be configured");
    }

    @Async("emailTaskExecutor")
    @SuppressWarnings("null")
    public void sendWeeklyReminder(User user) {
        Locale locale = resolveLocale(user.getLanguage());
        try {
            String subject  = msg("email.weekly.subject", null, locale);
            String greeting = msg("email.weekly.greeting", new Object[]{user.getUsername()}, locale);
            String question = msg("email.weekly.question", null, locale);
            String body     = msg("email.weekly.body",     null, locale);
            String cta      = msg("email.weekly.cta",      null, locale);
            String footer   = msg("email.weekly.footerPrefix",    null, locale);
            String profile  = msg("email.weekly.profileLinkText", null, locale);

            String html = loadTemplate()
                    .replace("{{emailGreeting}}",         greeting)
                    .replace("{{emailQuestion}}",         question)
                    .replace("{{emailBody}}",             body)
                    .replace("{{emailCtaLabel}}",         cta)
                    .replace("{{emailFooterPrefix}}",     footer)
                    .replace("{{emailProfileLinkText}}",  profile)
                    .replace("{{baseUrl}}",               baseUrl);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email semanal enviado para {} (lang={})", user.getEmail(), user.getLanguage());
        } catch (MessagingException | IOException e) {
            log.error("Falha ao enviar email semanal para {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @NonNull
    @SuppressWarnings("null")
    private String msg(String key, Object[] args, Locale locale) {
        return messageSource.getMessage(key, args, locale);
    }

    private Locale resolveLocale(String language) {
        if (language == null) return Locale.forLanguageTag("pt");
        return switch (language) {
            case "en" -> Locale.ENGLISH;
            case "es" -> Locale.forLanguageTag("es");
            default   -> Locale.forLanguageTag("pt");
        };
    }

    private String loadTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);
        byte[] bytes = resource.getInputStream().readAllBytes();

        return new String(bytes, StandardCharsets.UTF_8);
    }
}
