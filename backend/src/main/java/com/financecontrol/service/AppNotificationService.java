package com.financecontrol.service;

import com.financecontrol.dto.response.AppNotificationResponse;
import com.financecontrol.entity.AppNotification;
import com.financecontrol.entity.Goal;
import com.financecontrol.entity.GoalNotificationLog;
import com.financecontrol.enums.AppNotificationType;
import com.financecontrol.enums.GoalNotificationType;
import com.financecontrol.enums.GoalStatus;
import com.financecontrol.enums.GoalType;
import com.financecontrol.repository.AppNotificationRepository;
import com.financecontrol.repository.GoalRepository;
import com.financecontrol.repository.GoalNotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppNotificationService {

    private final AppNotificationRepository appNotificationRepository;
    private final GoalRepository goalRepository;
    private final GoalNotificationLogRepository goalNotificationLogRepository;
    private final GoalService goalService;

    @Transactional
    public List<AppNotificationResponse> checkGoalImpact(Long userId,
                                                         Long transactionId) {
        List<Goal> activeGoals = goalRepository.findByUserIdAndStatus(userId, GoalStatus.ACTIVE);
        List<AppNotificationResponse> result = new ArrayList<>();

        for (Goal goal : activeGoals) {
            double target = goal.getTargetAmount() != null ? goal.getTargetAmount() : 0.0;
            if (target <= 0) continue;

            double current = goalService.calculateCurrentAmount(goal);
            double pct = (current / target) * 100.0;
            boolean isExpense = goal.getType() == GoalType.EXPENSE_LIMIT;

            if (isExpense && pct >= 100.0 && Boolean.TRUE.equals(goal.getNotifyOnExceed())) {
                tryCreate(userId, goal, transactionId, GoalNotificationType.EXCEEDED, AppNotificationType.GOAL_EXCEEDED)
                        .ifPresent(result::add);
            }
            if (!isExpense && pct >= 100.0 && Boolean.TRUE.equals(goal.getNotifyOnComplete())) {
                tryCreate(userId, goal, transactionId, GoalNotificationType.COMPLETED, AppNotificationType.GOAL_COMPLETED)
                        .ifPresent(result::add);
            }
            if (pct >= 90.0 && Boolean.TRUE.equals(goal.getNotifyAt90())) {
                tryCreate(userId, goal, transactionId, GoalNotificationType.MILESTONE_90, AppNotificationType.GOAL_MILESTONE_90)
                        .ifPresent(result::add);
            }
            if (pct >= 75.0 && Boolean.TRUE.equals(goal.getNotifyAt75())) {
                tryCreate(userId, goal, transactionId, GoalNotificationType.MILESTONE_75, AppNotificationType.GOAL_MILESTONE_75)
                        .ifPresent(result::add);
            }
            if (pct >= 50.0 && Boolean.TRUE.equals(goal.getNotifyAt50())) {
                tryCreate(userId, goal, transactionId, GoalNotificationType.MILESTONE_50, AppNotificationType.GOAL_MILESTONE_50)
                        .ifPresent(result::add);
            }
        }

        return result;
    }

    @Transactional
    @SuppressWarnings("null")
    public AppNotificationResponse createGoalNotification(Long userId, 
                                                          Long goalId,
                                                          String goalName,
                                                          AppNotificationType type,
                                                          Long transactionId) {
        AppNotification n = buildNotification(userId, type, goalId, goalName, transactionId);
        return AppNotificationResponse.from(appNotificationRepository.save(n));
    }

    public List<AppNotificationResponse> findAll(Long userId) {
        return appNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(AppNotificationResponse::from).toList();
    }

    public long getUnreadCount(Long userId) {
        return appNotificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    @SuppressWarnings("null")
    public void markAsRead(Long userId,
                           Long id) {
        appNotificationRepository.findById(id).ifPresent(n -> {
            if (n.getUserId().equals(userId)) {
                n.setRead(true);
                appNotificationRepository.save(n);
            }
        });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        appNotificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public AppNotificationResponse saveUserAction(Long userId,
                                                  String message,
                                                  String severity,
                                                  String link) {
        AppNotification n = new AppNotification();
        
        n.setUserId(userId);
        n.setType(AppNotificationType.USER_ACTION);
        n.setMessage(message);
        n.setSeverity(severity != null ? severity : "info");
        n.setLink(link);
        n.setRead(true);
        n.setCreatedAt(LocalDateTime.now());

        return AppNotificationResponse.from(appNotificationRepository.save(n));
    }

    @SuppressWarnings("null")
    private Optional<AppNotificationResponse> tryCreate(Long userId,
                                                        Goal goal,
            Long transactionId, GoalNotificationType logType, AppNotificationType appType) {

        if (goalNotificationLogRepository.existsByGoalIdAndNotificationType(goal.getId(), logType)) {
            return Optional.empty();
        }

        AppNotification saved = appNotificationRepository.save(
                buildNotification(userId, appType, goal.getId(), goal.getName(), transactionId));

        GoalNotificationLog entry = new GoalNotificationLog();
        entry.setGoalId(goal.getId());
        entry.setNotificationType(logType);
        entry.setSentAt(LocalDateTime.now());
        goalNotificationLogRepository.save(entry);

        log.info("Notificação in-app {} criada para meta '{}' (user={})", appType, goal.getName(), userId);
        return Optional.of(AppNotificationResponse.from(saved));
    }

    private AppNotification buildNotification(Long userId,
                                              AppNotificationType type,
                                              Long goalId,
                                              String goalName,
                                              Long transactionId) {
        AppNotification n = new AppNotification();

        n.setUserId(userId);
        n.setType(type);
        n.setGoalId(goalId);
        n.setGoalName(goalName);
        n.setTransactionId(transactionId);
        n.setLink("/pages/GoalDashboard.html?highlight=" + goalId);
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.now());

        return n;
    }
}
