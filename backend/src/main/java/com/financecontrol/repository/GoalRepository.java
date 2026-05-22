package com.financecontrol.repository;

import com.financecontrol.entity.Goal;
import com.financecontrol.enums.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Goal> findByStatus(GoalStatus status);
    List<Goal> findByUserIdAndStatus(Long userId, GoalStatus status);

    long countByUserId(Long userId);
    long countByUserIdAndStatus(Long userId, GoalStatus status);

    boolean existsByUserIdAndStatusAndEndDateAfter(Long userId, GoalStatus status, LocalDate date);
}
