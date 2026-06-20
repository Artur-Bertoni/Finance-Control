package com.financecontrol.service.finny;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financecontrol.entity.FinnyTip;
import com.financecontrol.enums.FinnyTipCategory;
import com.financecontrol.enums.TransactionType;
import com.financecontrol.repository.FinnyTipRepository;
import com.financecontrol.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FinnyLearningScheduler {

    private static final int    COMPARE_DAYS    = 30;
    private static final double DROP_THRESHOLD  = 0.90;
    private static final double IMPLICIT_REWARD = 0.15;

    private final FinnyTipRepository tipRepository;
    private final TransactionRepository transactionRepository;
    private final FinnyAgentService agentService;
    private final ObjectMapper objectMapper;
    private final ZoneId zoneId;

    public FinnyLearningScheduler(FinnyTipRepository tipRepository,
                                  TransactionRepository transactionRepository,
                                  FinnyAgentService agentService,
                                  ObjectMapper objectMapper,
                                  @Value("${app.scheduler.timezone:America/Sao_Paulo}") String timezone) {
        this.tipRepository = tipRepository;
        this.transactionRepository = transactionRepository;
        this.agentService = agentService;
        this.objectMapper = objectMapper;
        this.zoneId = ZoneId.of(timezone);
    }

    @Transactional
    @Scheduled(cron = "0 0 9 * * MON", zone = "${app.scheduler.timezone:America/Sao_Paulo}")
    public void learnFromBehavior() {
        LocalDateTime now = LocalDateTime.now(zoneId);
        LocalDateTime windowStart = now.minusDays(37);
        LocalDateTime windowEnd   = now.minusDays(COMPARE_DAYS);

        List<FinnyTip> tips = tipRepository.findByRuleKeyAndCreatedAtBetween("TOP_CATEGORY", windowStart, windowEnd);
        if (tips.isEmpty()) return;

        log.info("Finny: avaliando {} dica(s) TOP_CATEGORY para aprendizado implícito", tips.size());
        for (FinnyTip tip : tips) {
            try {
                evaluate(tip);
            } catch (Exception e) {
                log.warn("Finny: erro ao avaliar dica {} para aprendizado: {}", tip.getId(), e.getMessage());
            }
        }
    }

    private void evaluate(FinnyTip tip) {
        Long categoryId = extractCategoryId(tip.getParamsJson());
        if (categoryId == null) return;

        LocalDate tipDate = tip.getCreatedAt().toLocalDate();
        List<Long> cat = List.of(categoryId);

        double before = nz(transactionRepository.sumForGoalByCategories(
                tip.getUserId(), tipDate.minusDays(COMPARE_DAYS), tipDate.minusDays(1), TransactionType.DEBIT, cat));
        double after = nz(transactionRepository.sumForGoalByCategories(
                tip.getUserId(), tipDate, tipDate.plusDays(COMPARE_DAYS), TransactionType.DEBIT, cat));

        if (before > 0 && after < before * DROP_THRESHOLD) {
            agentService.nudgeWeight(tip.getUserId(), FinnyTipCategory.BUDGET, IMPLICIT_REWARD);
            log.info("Finny: usuário {} reduziu gastos na categoria {} (de {} para {}) - reforçando BUDGET",
                    tip.getUserId(), categoryId, before, after);
        }
    }

    private Long extractCategoryId(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            Map<String, Object> params = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            Object raw = params.get("categoryId");
            return raw instanceof Number n ? n.longValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static double nz(Double v) {
        return v != null ? v : 0.0;
    }
}
