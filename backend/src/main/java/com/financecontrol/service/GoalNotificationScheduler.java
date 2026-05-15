package com.financecontrol.service;

import com.financecontrol.entity.FinancialGoal;
import com.financecontrol.entity.GoalNotificationLog;
import com.financecontrol.entity.User;
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
    private final UserRepository                userRepository;
    private final ZoneId                        schedulerZone;

    public GoalNotificationScheduler(
            FinancialGoalRepository goalRepository,
            GoalNotificationLogRepository logRepository,
            FinancialGoalService goalService,
            EmailService emailService,
            UserRepository userRepository,
            @Value("${app.scheduler.timezone:America/Sao_Paulo}") String timezone) {
        this.goalRepository = goalRepository;
        this.logRepository  = logRepository;
        this.goalService    = goalService;
        this.emailService   = emailService;
        this.userRepository = userRepository;
        this.schedulerZone  = ZoneId.of(timezone);
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
        double pct     = percentOf(current, goal.getTargetAmount());

        User user = userRepository.findById(goal.getUserId()).orElse(null);
        if (user == null || !user.isGoalEmailNotificationEnabled()) return;

        if (today.isAfter(goal.getEndDate())) {
            finalizeGoal(goal, user, pct, current);
            return;
        }

        checkMilestones(goal, user, pct, current);
        checkCompletion(goal, user, pct, current);
        checkDeadlineWarning(goal, user, today, current);
    }

    // ── End-date finalisation ─────────────────────────────────────────────

    private void finalizeGoal(FinancialGoal goal, User user, double pct, double current) {
        boolean isExpenseLimit = goal.getType() == GoalType.EXPENSE_LIMIT;
        boolean success        = isExpenseLimit ? pct < 100.0 : pct >= 100.0;
        goal.setStatus(success ? GoalStatus.COMPLETED : GoalStatus.EXPIRED);
        goalRepository.save(goal);
        if (success && Boolean.TRUE.equals(goal.getNotifyOnComplete())) {
            sendIfNotSent(goal, user, GoalNotificationType.COMPLETED, current);
        }
    }

    // ── Active-goal checks ────────────────────────────────────────────────

    private void checkMilestones(FinancialGoal goal, User user, double pct, double current) {
        if (Boolean.TRUE.equals(goal.getNotifyAt50()) && pct >= 50.0) {
            sendIfNotSent(goal, user, GoalNotificationType.MILESTONE_50, current);
        }
        if (Boolean.TRUE.equals(goal.getNotifyAt75()) && pct >= 75.0) {
            sendIfNotSent(goal, user, GoalNotificationType.MILESTONE_75, current);
        }
        if (Boolean.TRUE.equals(goal.getNotifyAt90()) && pct >= 90.0) {
            sendIfNotSent(goal, user, GoalNotificationType.MILESTONE_90, current);
        }
    }

    private void checkCompletion(FinancialGoal goal, User user, double pct, double current) {
        boolean isExpenseLimit = goal.getType() == GoalType.EXPENSE_LIMIT;
        if (!isExpenseLimit && pct >= 100.0) {
            goal.setStatus(GoalStatus.COMPLETED);
            goalRepository.save(goal);
            if (Boolean.TRUE.equals(goal.getNotifyOnComplete())) {
                sendIfNotSent(goal, user, GoalNotificationType.COMPLETED, current);
            }
        } else if (isExpenseLimit && pct >= 100.0 && Boolean.TRUE.equals(goal.getNotifyOnExceed())) {
            sendIfNotSent(goal, user, GoalNotificationType.EXCEEDED, current);
        }
    }

    private void checkDeadlineWarning(FinancialGoal goal, User user, LocalDate today, double current) {
        if (Boolean.TRUE.equals(goal.getNotifyOnDeadline())
                && !today.isBefore(goal.getEndDate().minusDays(7))) {
            sendIfNotSent(goal, user, GoalNotificationType.DEADLINE_WARNING, current);
        }
    }

    // ── Notification dispatch ─────────────────────────────────────────────

    private void sendIfNotSent(FinancialGoal goal, User user, GoalNotificationType type, double current) {
        if (logRepository.existsByGoalIdAndNotificationType(goal.getId(), type)) return;

        emailService.sendGoalNotification(user, goal, type, current);

        GoalNotificationLog entry = new GoalNotificationLog();
        entry.setGoalId(goal.getId());
        entry.setNotificationType(type);
        entry.setSentAt(LocalDateTime.now());
        logRepository.save(entry);

        log.info("Notificação {} registrada para meta '{}' (user={})", type, goal.getName(), user.getEmail());
    }

    // ── Utility ───────────────────────────────────────────────────────────

    private double percentOf(double current, Double target) {
        return target != null && target > 0 ? (current / target) * 100.0 : 0.0;
    }
}
