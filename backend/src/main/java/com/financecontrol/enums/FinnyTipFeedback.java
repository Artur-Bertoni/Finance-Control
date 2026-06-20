package com.financecontrol.enums;

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
