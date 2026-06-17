package com.financecontrol.enums;

/**
 * Feedback explícito que o usuário pode dar a uma dica.
 * Cada valor carrega o status resultante e o quanto move o peso adaptativo
 * da categoria (positivo reforça, negativo enfraquece).
 */
public enum FinnyTipFeedback {
    HELPFUL    (FinnyTipStatus.HELPFUL,     +0.25),
    NOT_HELPFUL(FinnyTipStatus.NOT_HELPFUL, -0.20),
    DISMISSED  (FinnyTipStatus.DISMISSED,   -0.10);

    private final FinnyTipStatus status;
    private final double weightDelta;

    FinnyTipFeedback(FinnyTipStatus status, double weightDelta) {
        this.status = status;
        this.weightDelta = weightDelta;
    }

    public FinnyTipStatus status() {
        return status;
    }

    public double weightDelta() {
        return weightDelta;
    }
}
