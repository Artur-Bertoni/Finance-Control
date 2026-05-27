package com.financecontrol.repository;

import com.financecontrol.entity.Goal;
import com.financecontrol.enums.GoalStatus;
import com.financecontrol.enums.GoalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT g FROM Goal g WHERE g.userId = :userId AND g.status <> 'ARCHIVED' " +
           "AND LOWER(g.name) = LOWER(:name) AND g.type = :type AND g.targetAmount = :targetAmount " +
           "AND ((:startDate IS NULL AND g.startDate IS NULL) OR g.startDate = :startDate) " +
           "AND ((:endDate IS NULL AND g.endDate IS NULL) OR g.endDate = :endDate)")
    List<Goal> findPotentialDuplicates(@Param("userId") Long userId,
                                       @Param("name") String name,
                                       @Param("type") GoalType type,
                                       @Param("targetAmount") Double targetAmount,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);
}
