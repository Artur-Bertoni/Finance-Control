package com.financecontrol.dto.request;

import com.financecontrol.enums.FinnyTipFeedback;

/** Feedback do usuário sobre uma dica: HELPFUL, NOT_HELPFUL ou DISMISSED. */
public record FinnyFeedbackRequest(FinnyTipFeedback feedback) {}
