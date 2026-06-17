package com.financecontrol.repository;

import com.financecontrol.entity.FinnyTip;
import com.financecontrol.enums.FinnyTipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinnyTipRepository extends JpaRepository<FinnyTip, Long> {

    /** Conjunto "ativo" (NEW = a popar / SHOWN = no modal aguardando feedback), por relevância. */
    List<FinnyTip> findByUserIdAndStatusInOrderByScoreDesc(Long userId, Collection<FinnyTipStatus> statuses);

    /** Histórico: tudo que já foi mostrado (status != NEW), mais recentes primeiro. */
    List<FinnyTip> findByUserIdAndStatusNotOrderByCreatedAtDesc(Long userId, FinnyTipStatus status);

    /** Dedup: já existe uma dica ATIVA (NEW/SHOWN) desta regra para o usuário? */
    Optional<FinnyTip> findFirstByUserIdAndRuleKeyAndStatusInOrderByCreatedAtDesc(
            Long userId, String ruleKey, Collection<FinnyTipStatus> statuses);

    /** Supressão: a regra recebeu QUALQUER feedback recentemente? (não regerar/repopar por um tempo) */
    boolean existsByUserIdAndRuleKeyAndFeedbackAtAfter(Long userId, String ruleKey, LocalDateTime since);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, FinnyTipStatus status);

    /** Contagem agrupada por categoria para o painel de progresso. */
    @Query("SELECT t.category, COUNT(t) FROM FinnyTip t WHERE t.userId = :userId GROUP BY t.category")
    List<Object[]> countByCategory(@Param("userId") Long userId);

    /** Dicas de uma regra geradas num intervalo (global) — varredura do aprendizado implícito. */
    List<FinnyTip> findByRuleKeyAndCreatedAtBetween(String ruleKey, LocalDateTime start, LocalDateTime end);
}
