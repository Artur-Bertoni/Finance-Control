package com.financecontrol.service;

import com.financecontrol.entity.FinancialGoal;
import com.financecontrol.entity.GoalNotificationLog;
import com.financecontrol.entity.User;
import com.financecontrol.enums.AppNotificationType;
import com.financecontrol.enums.GoalNotificationType;
import com.financecontrol.enums.GoalStatus;
import com.financecontrol.enums.GoalType;
import com.financecontrol.repository.FinancialGoalRepository;
import com.financecontrol.repository.GoalNotificationLogRepository;
import com.financecontrol.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@Slf4j
public class GoalNotificationScheduler {

    private final FinancialGoalRepository       goalRepository;
    private final GoalNotificationLogRepository logRepository;
    private final FinancialGoalService          goalService;
    private final EmailService                  emailService;
    private final AppNotificationService        notificationService;
    private final UserRepository                userRepository;
    private final ZoneId                        schedulerZone;

    public GoalNotificationScheduler(
            FinancialGoalRepository goalRepository,
            GoalNotificationLogRepository logRepository,
            FinancialGoalService goalService,
            EmailService emailService,
            AppNotificationService notificationService,
            UserRepository userRepository,
            @Value("${app.scheduler.timezone:America/Sao_Paulo}") String timezone) {
        this.goalRepository      = goalRepository;
        this.logRepository       = logRepository;
        this.goalService         = goalService;
        this.emailService        = emailService;
        this.notificationService = notificationService;
        this.userRepository      = userRepository;
        this.schedulerZone       = ZoneId.of(timezone);
    }

    @Scheduled(cron = "0 30 8 * * *", zone = "${app.scheduler.timezone:America/Sao_Paulo}")
    @Transactional
    public void processGoals() {
        LocalDate today = LocalDate.now(schedulerZone);
        List<FinancialGoal> active = goalRepository.findByStatus(GoalStatus.ACTIVE);
        if (active.isEmpty()) return;

        log.info("Processando {} meta(s) ativa(s) (data={})", active.size(), today);
        for (FinancialGoal goal : active) {
            try {
                processGoal(goal, today);
            } catch (Exception e) {
                log.error("Erro ao processar meta id={}: {}", goal.getId(), e.getMessage());
            }
        }
    }

    @SuppressWarnings("null")
    private void processGoal(FinancialGoal goal, LocalDate today) {
        double current = goalService.calculateCurrentAmount(goal);

        User user = userRepository.findById(goal.getUserId()).orElse(null);
        if (user == null) return;

        if (today.isAfter(goal.getEndDate())) {
            finalizeGoal(goal, user, current);
            return;
        }

        checkDeadlineWarning(goal, user, today, current);
    }

    private void finalizeGoal(FinancialGoal goal, User user, double current) {
        boolean isExpenseLimit = goal.getType() == GoalType.EXPENSE_LIMIT;
        double pct = percentOf(current, goal.getTargetAmount());
        boolean success = isExpenseLimit ? pct < 100.0 : pct >= 100.0;

        goal.setStatus(success ? GoalStatus.COMPLETED : GoalStatus.EXPIRED);
        goalRepository.save(goal);

        if (success && Boolean.TRUE.equals(goal.getNotifyOnComplete())) {
            sendInAppIfNotSent(goal, user, GoalNotificationType.COMPLETED, AppNotificationType.GOAL_COMPLETED);
        }
    }

    private void checkDeadlineWarning(FinancialGoal goal, User user, LocalDate today, double current) {
        if (!Boolean.TRUE.equals(goal.getNotifyOnDeadline())) return;
        if (today.isBefore(goal.getEndDate().minusDays(7))) return;
        if (logRepository.existsByGoalIdAndNotificationType(goal.getId(), GoalNotificationType.DEADLINE_WARNING)) return;

        if (user.isGoalEmailNotificationEnabled()) {
            emailService.sendGoalNotification(user, goal, GoalNotificationType.DEADLINE_WARNING, current);
        }

        notificationService.createGoalNotification(
                user.getId(), goal.getId(), goal.getName(),
                AppNotificationType.GOAL_DEADLINE_WARNING, null);

        GoalNotificationLog entry = new GoalNotificationLog();
        entry.setGoalId(goal.getId());
        entry.setNotificationType(GoalNotificationType.DEADLINE_WARNING);
        entry.setSentAt(LocalDateTime.now());
        logRepository.save(entry);

        log.info("Aviso de prazo registrado para meta '{}' (user={})", goal.getName(), user.getEmail());
    }

    @SuppressWarnings("null")
    private void sendInAppIfNotSent(FinancialGoal goal, User user,
                                    GoalNotificationType logType, AppNotificationType appType) {
        if (logRepository.existsByGoalIdAndNotificationType(goal.getId(), logType)) return;

        notificationService.createGoalNotification(
                user.getId(), goal.getId(), goal.getName(), appType, null);

        GoalNotificationLog entry = new GoalNotificationLog();
        entry.setGoalId(goal.getId());
        entry.setNotificationType(logType);
        entry.setSentAt(LocalDateTime.now());
        logRepository.save(entry);

        log.info("Notificação {} registrada para meta '{}' (user={})", appType, goal.getName(), user.getEmail());
    }

    private double percentOf(double current, Double target) {
        return target != null && target > 0 ? (current / target) * 100.0 : 0.0;
    }
}
