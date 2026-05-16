package com.financecontrol.repository;

import com.financecontrol.entity.FinancialGoal;
import com.financecontrol.enums.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, Long> {
    List<FinancialGoal> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<FinancialGoal> findByStatus(GoalStatus status);
    List<FinancialGoal> findByUserIdAndStatus(Long userId, GoalStatus status);

    long countByUserId(Long userId);
    long countByUserIdAndStatus(Long userId, GoalStatus status);

    boolean existsByUserIdAndStatusAndEndDateAfter(Long userId, GoalStatus status, LocalDate date);
}
