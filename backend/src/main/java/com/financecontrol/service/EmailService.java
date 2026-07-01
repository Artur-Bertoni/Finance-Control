package com.financecontrol.service;

import com.financecontrol.entity.Goal;
import com.financecontrol.entity.User;
import com.financecontrol.entity.UserFeedback;
import com.financecontrol.enums.FeedbackType;
import com.financecontrol.enums.GoalStatus;
import com.financecontrol.enums.GoalType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
public class EmailService {

    private static final String TEMPLATE_WEEKLY       = "templates/weekly-reminder.html";
    private static final String TEMPLATE_GOAL         = "templates/goal-notification.html";
    private static final String TEMPLATE_VERIFICATION = "templates/email-verification.html";
    private static final String TEMPLATE_FEEDBACK     = "templates/feedback-notification.html";

    private final JavaMailSender mailSender;
    private final MessageSource messageSource;
    private final String baseUrl;
    private final String mailFrom;

    public EmailService(JavaMailSender mailSender,
                        MessageSource messageSource,
                        @Value("${app.base-url}") String baseUrl,
                        @Value("${app.mail.from}") String mailFrom) {
        this.mailSender = mailSender;
        this.messageSource = messageSource;
        this.baseUrl = Objects.requireNonNull(baseUrl,  "app.base-url must be configured");
        this.mailFrom = Objects.requireNonNull(mailFrom, "app.mail.from must be configured");
    }

    @Async("emailTaskExecutor")
    public void sendVerificationEmail(User user,
                                      String token) {
        try {
            doSendVerification(user, token);
            log.info("Email de verificação enviado para userId={}", user.getId());
        } catch (Exception e) {
            log.error("Falha ao enviar email de verificação para userId={}: {}", user.getId(), e.getMessage());
        }
    }

    public void sendTestVerificationEmail(User user) throws MessagingException, IOException {
        doSendVerification(user, "test-verification-token");
    }

    private void doSendVerification(User user,
                                    String token) throws MessagingException, IOException {
        Locale locale = resolveLocale(user.getLanguage());
        String subject = msg("email.verify.subject", null, locale);
        String link = baseUrl + "/api/auth/verify-email?token=" + token;

        String html = loadTemplate(TEMPLATE_VERIFICATION)
                .replace("{{emailGreeting}}",  msg("email.verify.greeting",  new Object[]{user.getUsername()}, locale))
                .replace("{{emailBody}}",       msg("email.verify.body",       null, locale))
                .replace("{{emailCtaLabel}}",   msg("email.verify.cta",        null, locale))
                .replace("{{emailFooterNote}}", msg("email.verify.footerNote", null, locale))
                .replace("{{verifyUrl}}",       link)
                .replace("{{baseUrl}}",         baseUrl);

        sendMimeMessage(user.getEmail(), subject, html);
    }

    @Async("emailTaskExecutor")
    public void sendWeeklyReminder(User user) {
        try {
            doSendWeekly(user);
            log.info("Email semanal enviado para userId={} (lang={})", user.getId(), user.getLanguage());
        } catch (Exception e) {
            log.error("Falha ao enviar email semanal para userId={}: {}", user.getId(), e.getMessage());
        }
    }

    public void sendTestEmail(User user) throws MessagingException, IOException {
        doSendWeekly(user);
    }

    public void sendTestGoalDeadlineEmail(User user) throws MessagingException, IOException {
        Goal sample = buildSampleGoal();
        doSendGoalDeadline(user, sample, sample.getTargetAmount() * 0.40);
    }

    private void doSendWeekly(User user) throws MessagingException, IOException {
        Locale locale = resolveLocale(user.getLanguage());

        String subject  = msg("email.weekly.subject", null, locale);
        String subtitle = msg("email.weekly.subtitle", null, locale);
        String greeting = msg("email.weekly.greeting", new Object[]{user.getUsername()},  locale);
        String question = msg("email.weekly.question", null, locale);
        String body     = msg("email.weekly.body", null, locale);
        String cta      = msg("email.weekly.cta", null, locale);
        String footer   = msg("email.weekly.footerPrefix", null, locale);
        String profile  = msg("email.weekly.profileLinkText", null, locale);

        String html = loadTemplate(TEMPLATE_WEEKLY)
                .replace("{{emailSubtitle}}", subtitle)
                .replace("{{emailGreeting}}", greeting)
                .replace("{{emailQuestion}}", question)
                .replace("{{emailBody}}", body)
                .replace("{{emailCtaLabel}}", cta)
                .replace("{{emailFooterPrefix}}", footer)
                .replace("{{emailProfileLinkText}}", profile)
                .replace("{{baseUrl}}", baseUrl);

        sendMimeMessage(user.getEmail(), subject, html);
    }

    private void doSendGoalDeadline(User user,
                                    Goal goal,
                                    double current)
            throws MessagingException, IOException {
        Locale locale = resolveLocale(user.getLanguage());

        String subject     = msg("email.goal.subject.deadline", null,                                                locale);
        String subtitle    = msg("email.goal.subtitle",         null,                                                locale);
        String greeting    = msg("email.goal.greeting",         new Object[]{user.getUsername()},                    locale);
        String title       = msg("email.goal.deadline.title",   new Object[]{goal.getName()},                        locale);
        String body        = msg("email.goal.deadline.body",    new Object[]{formatDate(goal.getEndDate(), locale)}, locale);
        String cta         = msg("email.goal.cta",              null,                                                locale);
        String footer      = msg("email.goal.footerPrefix",     null,                                                locale);
        String profile     = msg("email.goal.profileLinkText",  null,                                                locale);
        String progressLbl = msg("email.goal.progress",         null,                                                locale);
        String targetLbl   = msg("email.goal.target",           null,                                                locale);

        double pct = goal.getTargetAmount() != null && goal.getTargetAmount() > 0
                ? (current / goal.getTargetAmount()) * 100.0
                : 0.0;

        String html = loadTemplate(TEMPLATE_GOAL)
                .replace("{{emailSubtitle}}",        subtitle)
                .replace("{{emailGreeting}}",        greeting)
                .replace("{{emailTitle}}",           title)
                .replace("{{emailBody}}",            body)
                .replace("{{emailProgressBar}}",     buildProgressBar(pct, goal.getType()))
                .replace("{{emailProgressLabel}}",   progressLbl)
                .replace("{{emailCurrentAmount}}",   String.format("%.2f", current))
                .replace("{{emailTargetLabel}}",     targetLbl)
                .replace("{{emailTargetAmount}}",    String.format("%.2f", goal.getTargetAmount()))
                .replace("{{emailCtaLabel}}",        cta)
                .replace("{{emailFooterPrefix}}",    footer)
                .replace("{{emailProfileLinkText}}", profile)
                .replace("{{baseUrl}}",              baseUrl);

        sendMimeMessage(user.getEmail(), subject, html);
    }

    @Async("emailTaskExecutor")
    public void sendFeedbackNotification(User admin, User sender, UserFeedback feedback) {
        try {
            doSendFeedback(admin, sender, feedback);
            log.info("Notificação de feedback enviada para adminId={}", admin.getId());
        } catch (Exception e) {
            log.error("Falha ao enviar notificação de feedback: {}", e.getMessage());
        }
    }

    public void sendTestFeedbackEmail(User user) throws MessagingException, IOException {
        Locale locale = resolveLocale(user.getLanguage());
        UserFeedback sample = new UserFeedback();
        sample.setUser(user);
        sample.setType(FeedbackType.SUGGESTION);
        sample.setMessage(msg("email.feedback.sampleMessage", null, locale));
        sample.setNpsScore(9);
        doSendFeedback(user, user, sample);
    }

    private void doSendFeedback(User admin,
                                User sender,
                                UserFeedback feedback) throws MessagingException, IOException {
        Locale locale    = resolveLocale(admin.getLanguage());
        String typeLabel = msg("email.feedback." + feedbackTypeKey(feedback.getType()), null, locale);
        String npsText   = feedback.getNpsScore() != null ? feedback.getNpsScore() + " / 10" : "-";

        String html = loadTemplate(TEMPLATE_FEEDBACK)
                .replace("{{emailSubtitle}}",     msg("email.feedback.subtitle",    null, locale))
                .replace("{{emailSenderLabel}}",  msg("email.feedback.sender",      null, locale))
                .replace("{{emailTypeLabel}}",    msg("email.feedback.typeLabel",   null, locale))
                .replace("{{emailNpsLabel}}",     msg("email.feedback.nps",         null, locale))
                .replace("{{emailMessageLabel}}", msg("email.feedback.message",     null, locale))
                .replace("{{emailCtaLabel}}",     msg("email.feedback.cta",         null, locale))
                .replace("{{senderName}}",        sender.getUsername())
                .replace("{{senderEmail}}",       sender.getEmail())
                .replace("{{feedbackType}}",      typeLabel)
                .replace("{{npsScore}}",          npsText)
                .replace("{{feedbackMessage}}",   feedback.getMessage())
                .replace("{{baseUrl}}",           baseUrl);

        sendMimeMessage(admin.getEmail(), msg("email.feedback.subject", null, locale), html);
    }

    private String feedbackTypeKey(FeedbackType type) {
        return switch (type) {
            case SUGGESTION -> "suggestion";
            case BUG        -> "bug";
            case GENERAL    -> "general";
        };
    }

    private Goal buildSampleGoal() {
        Goal g = new Goal();
        g.setName("Meta Exemplo");
        g.setType(GoalType.SAVINGS);
        g.setStatus(GoalStatus.ACTIVE);
        g.setTargetAmount(5000.0);
        g.setStartDate(LocalDate.now().withDayOfMonth(1));
        g.setEndDate(LocalDate.now().plusMonths(3));
        g.setCreatedAt(LocalDateTime.now());
        return g;
    }

    private String buildProgressBar(double pct,
                                    GoalType goalType) {
        String color   = progressBarColor(pct, goalType);
        double display = Math.min(pct, 100.0);

        return String.format(
                "<div style='background:#E5E7EB;border-radius:8px;height:12px;overflow:hidden;margin:16px 0;'>" +
                "<div style='background:%s;height:12px;width:%.1f%%;border-radius:8px;'></div></div>" +
                "<p style='margin:0;font-size:13px;color:#6B7280;text-align:right;'>%.1f%%</p>",
                color, display, Math.min(pct, 999.9));
    }

    private String progressBarColor(double pct,
                                    GoalType goalType) {
        if (goalType == GoalType.EXPENSE_LIMIT) {
            if (pct >= 90) return "#EF4444";
            if (pct >= 75) return "#F97316";
            if (pct >= 50) return "#EAB308";
            return "#2E7D32";
        }
        return pct >= 100 ? "#2E7D32" : "#3B82F6";
    }

    @SuppressWarnings("null")
    private void sendMimeMessage(String to,
                                 String subject,
                                 String html) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(mailFrom);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        helper.addInline("emailLogo", new ClassPathResource("templates/logo.png"), "image/png");

        mailSender.send(message);
    }

    @SuppressWarnings("null")
    private String loadTemplate(String templatePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(templatePath);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private String formatDate(LocalDate date, Locale locale) {
        if (date == null) return "";
        String pattern = "en".equals(locale.getLanguage()) ? "MM/dd/yyyy" : "dd/MM/yyyy";
        return date.format(DateTimeFormatter.ofPattern(pattern, locale));
    }

    @NonNull
    private Locale resolveLocale(String language) {
        if (language == null) 
            return Objects.requireNonNull(Locale.forLanguageTag("pt"));

        return Objects.requireNonNull(switch (language) {
            case "en" -> Locale.ENGLISH;
            case "es" -> Locale.forLanguageTag("es");
            default   -> Locale.forLanguageTag("pt");
        });
    }

    @NonNull
    @SuppressWarnings("null")
    private String msg(String key, Object[] args, Locale locale) {
        return messageSource.getMessage(key, args, locale);
    }
}
