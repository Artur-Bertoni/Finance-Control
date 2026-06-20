package com.financecontrol.repository;

import com.financecontrol.entity.FinnyTip;
import com.financecontrol.enums.FinnyTipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FinnyTipRepository extends JpaRepository<FinnyTip, Long> {

    List<FinnyTip> findByUserIdAndStatusInOrderByScoreDesc(Long userId, Collection<FinnyTipStatus> statuses);

    List<FinnyTip> findByUserIdAndStatusNotOrderByCreatedAtDesc(Long userId, FinnyTipStatus status);

    Optional<FinnyTip> findFirstByUserIdAndRuleKeyAndStatusInOrderByCreatedAtDesc(
            Long userId, String ruleKey, Collection<FinnyTipStatus> statuses);

    boolean existsByUserIdAndRuleKeyAndFeedbackAtAfter(Long userId, String ruleKey, LocalDateTime since);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, FinnyTipStatus status);

    @Query("SELECT t.category, COUNT(t) FROM FinnyTip t WHERE t.userId = :userId GROUP BY t.category")
    List<Object[]> countByCategory(@Param("userId") Long userId);

    List<FinnyTip> findByRuleKeyAndCreatedAtBetween(String ruleKey, LocalDateTime start, LocalDateTime end);
}
