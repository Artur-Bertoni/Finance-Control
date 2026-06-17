package com.financecontrol.dto.response;

import com.financecontrol.entity.FinnyTip;
import com.financecontrol.enums.FinnyTipCategory;
import com.financecontrol.enums.FinnyTipStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Dica enviada ao frontend. Não traz texto: traz {@code ruleKey} + {@code params}
 * para o front renderizar no idioma atual (chave i18n {@code finnyTip_<ruleKey>}).
 */
public record FinnyTipResponse(
        Long id,
        String ruleKey,
        FinnyTipCategory category,
        String severity,
        double score,
        FinnyTipStatus status,
        Map<String, Object> params,
        LocalDateTime createdAt,
        LocalDateTime shownAt,
        LocalDateTime feedbackAt
) {
    /** {@code params} já desserializado pelo serviço (que tem o ObjectMapper). */
    public static FinnyTipResponse from(FinnyTip t, Map<String, Object> params) {
        return new FinnyTipResponse(
                t.getId(), t.getRuleKey(), t.getCategory(), t.getSeverity(), t.getScore(),
                t.getStatus(), params, t.getCreatedAt(), t.getShownAt(), t.getFeedbackAt());
    }
}
