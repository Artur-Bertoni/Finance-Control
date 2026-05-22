package com.financecontrol.repository;

import com.financecontrol.entity.GoalNotificationLog;
import com.financecontrol.enums.GoalNotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoalNotificationLogRepository extends JpaRepository<GoalNotificationLog, Long> {
    boolean existsByGoalIdAndNotificationType(Long goalId, GoalNotificationType notificationType);
}
