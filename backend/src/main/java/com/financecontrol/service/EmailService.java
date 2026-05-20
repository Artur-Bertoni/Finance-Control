package com.financecontrol.service;

import com.financecontrol.entity.FinancialGoal;
import com.financecontrol.entity.User;
import com.financecontrol.enums.GoalNotificationType;
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
import java.util.Locale;
import java.util.Objects;

@Service
@Slf4j
public class EmailService {

    private static final String TEMPLATE_WEEKLY = "templates/weekly-reminder.html";
    private static final String TEMPLATE_GOAL   = "templates/goal-notification.html";

    private final JavaMailSender mailSender;
    private final MessageSource  messageSource;
    private final String         baseUrl;
    private final String         mailFrom;

    public EmailService(JavaMailSender mailSender,
                        MessageSource messageSource,
                        @Value("${app.base-url}") String baseUrl,
                        @Value("${app.mail.from}") String mailFrom) {
        this.mailSender    = mailSender;
        this.messageSource = messageSource;
        this.baseUrl       = Objects.requireNonNull(baseUrl,  "app.base-url must be configured");
        this.mailFrom      = Objects.requireNonNull(mailFrom, "app.mail.from must be configured");
    }

    @Async("emailTaskExecutor")
    public void sendWeeklyReminder(User user) {
        try {
            doSendWeekly(user);
            log.info("Email semanal enviado para {} (lang={})", user.getEmail(), user.getLanguage());
        } catch (Exception e) {
            log.error("Falha ao enviar email semanal para {}: {}", user.getEmail(), e.getMessage());
        }
    }

    public void sendTestEmail(User user) throws MessagingException, IOException {
        doSendWeekly(user);
    }

    public void sendTestGoalEmail(User user, GoalNotificationType type) throws MessagingException, IOException {
        FinancialGoal sample = buildSampleGoal();
        double current = switch (type) {
            case MILESTONE_50     -> sample.getTargetAmount() * 0.50;
            case MILESTONE_75     -> sample.getTargetAmount() * 0.75;
            case MILESTONE_90     -> sample.getTargetAmount() * 0.90;
            case COMPLETED        -> sample.getTargetAmount();
            case DEADLINE_WARNING -> sample.getTargetAmount() * 0.40;
            case EXCEEDED         -> sample.getTargetAmount() * 1.10;
        };
        doSendGoal(user, sample, type, current);
    }

    private void doSendWeekly(User user) throws MessagingException, IOException {
        Locale locale = resolveLocale(user.getLanguage());

        String subject  = msg("email.weekly.subject",         null,                              locale);
        String subtitle = msg("email.weekly.subtitle",        null,                              locale);
        String greeting = msg("email.weekly.greeting",        new Object[]{user.getUsername()},  locale);
        String question = msg("email.weekly.question",        null,                              locale);
        String body     = msg("email.weekly.body",            null,                              locale);
        String cta      = msg("email.weekly.cta",             null,                              locale);
        String footer   = msg("email.weekly.footerPrefix",    null,                              locale);
        String profile  = msg("email.weekly.profileLinkText", null,                              locale);

        String html = loadTemplate(TEMPLATE_WEEKLY)
                .replace("{{emailSubtitle}}",        subtitle)
                .replace("{{emailGreeting}}",        greeting)
                .replace("{{emailQuestion}}",        question)
                .replace("{{emailBody}}",            body)
                .replace("{{emailCtaLabel}}",        cta)
                .replace("{{emailFooterPrefix}}",    footer)
                .replace("{{emailProfileLinkText}}", profile)
                .replace("{{baseUrl}}",              baseUrl);

        sendMimeMessage(user.getEmail(), subject, html);
    }

    @Async("emailTaskExecutor")
    public void sendGoalNotification(User user, FinancialGoal goal,
                                     GoalNotificationType type, double current) {
        try {
            doSendGoal(user, goal, type, current);
            log.info("Notificação de meta enviada para {} (tipo={})", user.getEmail(), type);
        } catch (Exception e) {
            log.error("Falha ao enviar notificação de meta para {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private void doSendGoal(User user, FinancialGoal goal,
                            GoalNotificationType type, double current)
            throws MessagingException, IOException {
        Locale locale  = resolveLocale(user.getLanguage());
        String typeKey = goalTypeKey(type);

        String subject     = msg("email.goal.subject." + typeKey,    null,                             locale);
        String subtitle    = msg("email.goal.subtitle",              null,                             locale);
        String greeting    = msg("email.goal.greeting",              new Object[]{user.getUsername()}, locale);
        String title       = msg("email.goal." + typeKey + ".title", new Object[]{goal.getName()},     locale);
        String body        = buildGoalBody(goal, type, locale);
        String cta         = msg("email.goal.cta",                   null,                             locale);
        String footer      = msg("email.goal.footerPrefix",          null,                             locale);
        String profile     = msg("email.goal.profileLinkText",       null,                             locale);
        String progressLbl = msg("email.goal.progress",              null,                             locale);
        String targetLbl   = msg("email.goal.target",                null,                             locale);

        double pct = goal.getTargetAmount() != null && goal.getTargetAmount() > 0
                ? (current / goal.getTargetAmount()) * 100.0 : 0.0;

        String html = loadTemplate(TEMPLATE_GOAL)
                .replace("{{emailSubtitle}}",        subtitle)
                .replace("{{emailGreeting}}",        greeting)
                .replace("{{emailTitle}}",           title)
                .replace("{{emailBody}}",            body)
                .replace("{{emailProgressBar}}",     buildProgressBar(pct, type, goal.getType()))
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

    private FinancialGoal buildSampleGoal() {
        FinancialGoal g = new FinancialGoal();
        g.setName("Meta Exemplo");
        g.setType(GoalType.SAVINGS);
        g.setStatus(GoalStatus.ACTIVE);
        g.setTargetAmount(5000.0);
        g.setStartDate(LocalDate.now().withDayOfMonth(1));
        g.setEndDate(LocalDate.now().plusMonths(3));
        g.setCreatedAt(LocalDateTime.now());
        return g;
    }

    private static final String GOAL_MILESTONE_BODY_KEY = "email.goal.milestone.body";

    private String buildGoalBody(FinancialGoal goal, GoalNotificationType type, Locale locale) {
        return switch (type) {
            case MILESTONE_50     -> msg(GOAL_MILESTONE_BODY_KEY, new Object[]{"50"},              locale);
            case MILESTONE_75     -> msg(GOAL_MILESTONE_BODY_KEY, new Object[]{"75"},              locale);
            case MILESTONE_90     -> msg(GOAL_MILESTONE_BODY_KEY, new Object[]{"90"},              locale);
            case COMPLETED        -> msg("email.goal.completed.body", null,                            locale);
            case DEADLINE_WARNING -> msg("email.goal.deadline.body",  new Object[]{goal.getEndDate()}, locale);
            case EXCEEDED         -> msg("email.goal.exceeded.body",  null,                            locale);
        };
    }

    private String buildProgressBar(double pct, GoalNotificationType type, GoalType goalType) {
        String color   = progressBarColor(pct, type, goalType);
        double display = Math.min(pct, 100.0);
        return String.format(
                "<div style='background:#E5E7EB;border-radius:8px;height:12px;overflow:hidden;margin:16px 0;'>" +
                "<div style='background:%s;height:12px;width:%.1f%%;border-radius:8px;'></div></div>" +
                "<p style='margin:0;font-size:13px;color:#6B7280;text-align:right;'>%.1f%%</p>",
                color, display, Math.min(pct, 999.9));
    }

    private String progressBarColor(double pct, GoalNotificationType type, GoalType goalType) {
        if (type == GoalNotificationType.EXCEEDED) return "#EF4444";
        if (goalType == GoalType.EXPENSE_LIMIT) {
            if (pct >= 90) return "#EF4444";
            if (pct >= 75) return "#F97316";
            if (pct >= 50) return "#EAB308";
            return "#2E7D32";
        }
        return pct >= 100 ? "#2E7D32" : "#3B82F6";
    }

    private String goalTypeKey(GoalNotificationType type) {
        return switch (type) {
            case MILESTONE_50     -> "milestone50";
            case MILESTONE_75     -> "milestone75";
            case MILESTONE_90     -> "milestone90";
            case COMPLETED        -> "completed";
            case DEADLINE_WARNING -> "deadline";
            case EXCEEDED         -> "exceeded";
        };
    }

    @SuppressWarnings("null")
    private void sendMimeMessage(String to, String subject, String html)
            throws MessagingException {
        log.info("[EMAIL] Preparando envio → de={} para={} assunto='{}'", mailFrom, to, subject);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(mailFrom);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        helper.addInline("emailLogo", new ClassPathResource("templates/logo.png"), "image/png");
        long start = System.currentTimeMillis();
        mailSender.send(message);
        log.info("[EMAIL] SMTP aceitou a mensagem → para={} ({}ms)", to, System.currentTimeMillis() - start);
    }

    @SuppressWarnings("null")
    private String loadTemplate(String templatePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(templatePath);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    @NonNull
    private Locale resolveLocale(String language) {
        if (language == null) return Objects.requireNonNull(Locale.forLanguageTag("pt"));
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
