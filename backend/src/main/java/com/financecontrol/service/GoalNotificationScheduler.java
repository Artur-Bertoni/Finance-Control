package com.financecontrol.service;

import com.financecontrol.entity.Goal;
import com.financecontrol.entity.GoalNotificationLog;
import com.financecontrol.entity.User;
import com.financecontrol.enums.AppNotificationType;
import com.financecontrol.enums.GoalNotificationType;
import com.financecontrol.enums.GoalStatus;
import com.financecontrol.enums.GoalType;
import com.financecontrol.repository.GoalRepository;
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

@Slf4j
@Component
public class GoalNotificationScheduler {

    private final GoalRepository goalRepository;
    private final GoalNotificationLogRepository goalNotificationLogRepository;
    private final GoalService goalService;
    private final AppNotificationService appNotificationService;
    private final UserRepository userRepository;
    private final ZoneId zoneId;

    public GoalNotificationScheduler(GoalRepository goalRepository,
                                     GoalNotificationLogRepository goalNotificationLogRepository,
                                     GoalService goalService,
                                     AppNotificationService appNotificationService,
                                     UserRepository userRepository,
                                     @Value("${app.scheduler.timezone:America/Sao_Paulo}") String timezone) {
        this.goalRepository = goalRepository;
        this.goalNotificationLogRepository = goalNotificationLogRepository;
        this.goalService = goalService;
        this.appNotificationService = appNotificationService;
        this.userRepository = userRepository;
        this.zoneId = ZoneId.of(timezone);
    }

    @Transactional
    @Scheduled(cron = "0 30 8 * * *", zone = "${app.scheduler.timezone:America/Sao_Paulo}")
    public void processGoals() {
        LocalDate today = LocalDate.now(zoneId);
        List<Goal> active = goalRepository.findByStatus(GoalStatus.ACTIVE);
        if (active.isEmpty()) return;

        log.info("Processando {} meta(s) ativa(s) (data={})", active.size(), today);
        for (Goal goal : active) {
            try {
                processGoal(goal, today);
            } catch (Exception e) {
                log.error("Erro ao processar meta id={}: {}", goal.getId(), e.getMessage());
            }
        }
    }

    @SuppressWarnings("null")
    private void processGoal(Goal goal,
                             LocalDate today) {
        double current = goalService.calculateCurrentAmount(goal);

        User user = userRepository.findById(goal.getUserId()).orElse(null);
        if (user == null) return;

        if (goal.getEndDate() != null && today.isAfter(goal.getEndDate())) {
            finalizeGoal(goal, user, current);
            return;
        }

        checkDeadlineWarning(goal, user, today);
    }

    private void finalizeGoal(Goal goal,
                              User user,
                              double current) {
        boolean isExpenseLimit = goal.getType() == GoalType.EXPENSE_LIMIT;
        double pct = percentOf(current, goal.getTargetAmount());
        boolean success = isExpenseLimit ? pct < 100.0 : pct >= 100.0;

        goal.setStatus(success ? GoalStatus.COMPLETED : GoalStatus.EXPIRED);
        goalRepository.save(goal);

        if (success && Boolean.TRUE.equals(goal.getNotifyOnComplete())) {
            sendInAppIfNotSent(goal, user, GoalNotificationType.COMPLETED, AppNotificationType.GOAL_COMPLETED);
        }
    }

    private void checkDeadlineWarning(Goal goal,
                                      User user,
                                      LocalDate today) {
        if (!Boolean.TRUE.equals(goal.getNotifyOnDeadline())) return;
        if (goal.getEndDate() == null) return;
        if (today.isBefore(goal.getEndDate().minusDays(7))) return;
        if (goalNotificationLogRepository.existsByGoalIdAndNotificationType(goal.getId(), GoalNotificationType.GOAL_DEADLINE_WARNING)) return;

        appNotificationService.createGoalNotification(
                user.getId(), goal.getId(), goal.getName(),
                AppNotificationType.GOAL_DEADLINE_WARNING, null);

        GoalNotificationLog entry = new GoalNotificationLog();
        entry.setGoalId(goal.getId());
        entry.setNotificationType(GoalNotificationType.GOAL_DEADLINE_WARNING);
        entry.setSentAt(LocalDateTime.now());
        goalNotificationLogRepository.save(entry);

        log.info("Aviso de prazo registrado para meta '{}' (user={})", goal.getName(), user.getEmail());
    }

    private void sendInAppIfNotSent(Goal goal,
                                    User user,
                                    GoalNotificationType logType,
                                    AppNotificationType appType) {
        if (goalNotificationLogRepository.existsByGoalIdAndNotificationType(goal.getId(), logType)) return;

        appNotificationService.createGoalNotification(
                user.getId(), goal.getId(), goal.getName(), appType, null);

        GoalNotificationLog entry = new GoalNotificationLog();
        entry.setGoalId(goal.getId());
        entry.setNotificationType(logType);
        entry.setSentAt(LocalDateTime.now());
        goalNotificationLogRepository.save(entry);

        log.info("Notificação {} registrada para meta '{}' (user={})", appType, goal.getName(), user.getEmail());
    }

    private double percentOf(double current,
                             Double target) {
        return target != null && target > 0 ? (current / target) * 100.0 : 0.0;
    }
}
