package com.financecontrol.dto.response;

import com.financecontrol.entity.FinnyTip;
import com.financecontrol.enums.FinnyTipCategory;
import com.financecontrol.enums.FinnyTipStatus;

import java.time.LocalDateTime;
import java.util.Map;

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
    public static FinnyTipResponse from(FinnyTip t, Map<String, Object> params) {
        return new FinnyTipResponse(
                t.getId(), t.getRuleKey(), t.getCategory(), t.getSeverity(), t.getScore(),
                t.getStatus(), params, t.getCreatedAt(), t.getShownAt(), t.getFeedbackAt());
    }
}
