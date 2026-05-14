package com.financecontrol.repository;

import com.financecontrol.entity.FinancialGoal;
import com.financecontrol.enums.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, Long> {
    List<FinancialGoal> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<FinancialGoal> findByStatus(GoalStatus status);
}
