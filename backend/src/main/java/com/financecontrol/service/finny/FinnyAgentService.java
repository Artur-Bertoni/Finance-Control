package com.financecontrol.service.finny;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financecontrol.dto.response.FinnyStatsResponse;
import com.financecontrol.dto.response.FinnyTipResponse;
import com.financecontrol.entity.FinnyTip;
import com.financecontrol.entity.FinnyTipPreference;
import com.financecontrol.enums.FinnyTipCategory;
import com.financecontrol.enums.FinnyTipFeedback;
import com.financecontrol.enums.FinnyTipStatus;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.FinnyTipPreferenceRepository;
import com.financecontrol.repository.FinnyTipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class FinnyAgentService {

    private static final int    MAX_TIPS               = 5;
    private static final int    FEEDBACK_SUPPRESS_DAYS = 7;
    private static final double WEIGHT_MIN            = 0.2;
    private static final double WEIGHT_MAX            = 3.0;
    private static final double WEIGHT_DEFAULT        = 1.0;

    private static final List<FinnyTipStatus> ACTIVE_STATUSES = List.of(FinnyTipStatus.NEW, FinnyTipStatus.SHOWN);
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final List<TipRule> rules;
    private final FinancialProfileService profileService;
    private final FinnyTipRepository tipRepository;
    private final FinnyTipPreferenceRepository preferenceRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<FinnyTipResponse> generateTips(Long userId, String lang) {
        FinancialProfile profile = profileService.build(userId);
        Map<FinnyTipCategory, Double> weights = loadWeights(userId);
        LocalDateTime suppressSince = LocalDateTime.now(ZONE).minusDays(FEEDBACK_SUPPRESS_DAYS);

        List<Scored> scored = new ArrayList<>();
        for (TipRule rule : rules) {
            for (TipCandidate c : rule.evaluate(profile)) {
                if (tipRepository.existsByUserIdAndRuleKeyAndFeedbackAtAfter(userId, c.ruleKey(), suppressSince)) {
                    continue;
                }
                double weight = weights.getOrDefault(c.category(), WEIGHT_DEFAULT);
                scored.add(new Scored(c, c.baseScore() * weight));
            }
        }
        scored.sort(Comparator.comparingDouble(Scored::score).reversed());

        for (Scored s : scored.stream().limit(MAX_TIPS).toList()) {
            boolean alreadyActive = tipRepository
                    .findFirstByUserIdAndRuleKeyAndStatusInOrderByCreatedAtDesc(userId, s.candidate().ruleKey(), ACTIVE_STATUSES)
                    .isPresent();
            if (!alreadyActive) persistNew(userId, s, lang);
        }

        return tipRepository.findByUserIdAndStatusInOrderByScoreDesc(userId, ACTIVE_STATUSES)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public FinnyTipResponse markShown(Long userId, @NonNull Long tipId) {
        FinnyTip tip = getOwned(userId, tipId);
        if (tip.getStatus() == FinnyTipStatus.NEW) {
            tip.setStatus(FinnyTipStatus.SHOWN);
            tip.setShownAt(LocalDateTime.now(ZONE));
            tipRepository.save(tip);
        }
        return toResponse(tip);
    }

    @Transactional
    public FinnyTipResponse recordFeedback(Long userId, @NonNull Long tipId, FinnyTipFeedback feedback) {
        FinnyTip tip = getOwned(userId, tipId);

        feedbackOf(tip.getStatus()).ifPresent(prev -> moveWeight(userId, tip.getCategory(), prev, false));

        tip.setStatus(feedback.status());
        tip.setFeedbackAt(LocalDateTime.now(ZONE));
        tipRepository.save(tip);

        moveWeight(userId, tip.getCategory(), feedback, true);

        log.info("Finny: feedback {} na dica {} (regra={}, user={})", feedback, tipId, tip.getRuleKey(), userId);
        return toResponse(tip);
    }

    @Transactional(readOnly = true)
    public List<FinnyTipResponse> getHistory(Long userId) {
        return tipRepository.findByUserIdAndStatusNotOrderByCreatedAtDesc(userId, FinnyTipStatus.NEW)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FinnyStatsResponse getStats(Long userId) {
        FinancialProfile profile = profileService.build(userId);

        Map<String, Long> byCategory = new LinkedHashMap<>();
        for (Object[] row : tipRepository.countByCategory(userId)) {
            byCategory.put(((FinnyTipCategory) row[0]).name(), ((Number) row[1]).longValue());
        }

        return new FinnyStatsResponse(
                tipRepository.countByUserId(userId),
                tipRepository.countByUserIdAndStatus(userId, FinnyTipStatus.HELPFUL),
                tipRepository.countByUserIdAndStatus(userId, FinnyTipStatus.NOT_HELPFUL),
                tipRepository.countByUserIdAndStatus(userId, FinnyTipStatus.DISMISSED),
                byCategory,
                profile.savingsRatePct(),
                profile.emergencyFundMonths(),
                profile.net(),
                profile.currentBalance());
    }

    @Transactional
    public void nudgeWeight(Long userId, FinnyTipCategory category, double delta) {
        FinnyTipPreference pref = pref(userId, category);
        pref.setWeight(clamp(pref.getWeight() + delta));
        pref.setUpdatedAt(LocalDateTime.now(ZONE));
        preferenceRepository.save(pref);
    }

    private void moveWeight(Long userId, FinnyTipCategory category, FinnyTipFeedback feedback, boolean apply) {
        FinnyTipPreference pref = pref(userId, category);
        int sign = apply ? 1 : -1;

        switch (feedback) {
            case HELPFUL     -> pref.setHelpfulCount(Math.max(0, pref.getHelpfulCount() + sign));
            case NOT_HELPFUL -> pref.setNotHelpfulCount(Math.max(0, pref.getNotHelpfulCount() + sign));
            case DISMISSED   -> pref.setDismissedCount(Math.max(0, pref.getDismissedCount() + sign));
        }

        pref.setWeight(clamp(pref.getWeight() + sign * feedback.weightDelta()));
        pref.setUpdatedAt(LocalDateTime.now(ZONE));
        preferenceRepository.save(pref);
    }

    private FinnyTipPreference pref(Long userId, FinnyTipCategory category) {
        return preferenceRepository.findByUserIdAndCategory(userId, category)
                .orElseGet(() -> {
                    FinnyTipPreference p = new FinnyTipPreference();
                    p.setUserId(userId);
                    p.setCategory(category);
                    p.setWeight(WEIGHT_DEFAULT);
                    return p;
                });
    }

    private static double clamp(double weight) {
        return Math.max(WEIGHT_MIN, Math.min(WEIGHT_MAX, weight));
    }

    private static Optional<FinnyTipFeedback> feedbackOf(FinnyTipStatus status) {
        return switch (status) {
            case HELPFUL     -> Optional.of(FinnyTipFeedback.HELPFUL);
            case NOT_HELPFUL -> Optional.of(FinnyTipFeedback.NOT_HELPFUL);
            case DISMISSED   -> Optional.of(FinnyTipFeedback.DISMISSED);
            default          -> Optional.empty();
        };
    }

    private FinnyTip getOwned(Long userId, @NonNull Long tipId) {
        return tipRepository.findById(tipId)
                .filter(t -> Objects.equals(t.getUserId(), userId))
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound.finnyTip"));
    }

    private Map<FinnyTipCategory, Double> loadWeights(Long userId) {
        return preferenceRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(FinnyTipPreference::getCategory, FinnyTipPreference::getWeight));
    }

    private void persistNew(Long userId, Scored s, String lang) {
        TipCandidate c = s.candidate();
        FinnyTip tip = new FinnyTip();
        tip.setUserId(userId);
        tip.setRuleKey(c.ruleKey());
        tip.setCategory(c.category());
        tip.setSeverity(c.severity());
        tip.setScore(s.score());
        tip.setStatus(FinnyTipStatus.NEW);
        tip.setLang(lang);
        tip.setParamsJson(writeParams(c.params()));
        tip.setCreatedAt(LocalDateTime.now(ZONE));
        tipRepository.save(tip);
    }

    private FinnyTipResponse toResponse(FinnyTip tip) {
        return FinnyTipResponse.from(tip, readParams(tip.getParamsJson()));
    }

    private String writeParams(Map<String, Object> params) {
        if (params == null || params.isEmpty()) return "{}";
        try {
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            log.warn("Finny: falha ao serializar params {}", params, e);
            return "{}";
        }
    }

    private Map<String, Object> readParams(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Finny: falha ao desserializar params '{}'", json, e);
            return Map.of();
        }
    }

    private record Scored(TipCandidate candidate, double score) {}
}
