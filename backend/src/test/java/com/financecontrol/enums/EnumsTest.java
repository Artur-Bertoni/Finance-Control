package com.financecontrol.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EnumsTest {

    // ── TransactionType.fromCode ─────────────────────────────────────────────

    @Test
    void transactionType_fromCode_1_retornaDebit() {
        assertThat(TransactionType.fromCode(1)).isEqualTo(TransactionType.DEBIT);
    }

    @Test
    void transactionType_fromCode_2_retornaCredit() {
        assertThat(TransactionType.fromCode(2)).isEqualTo(TransactionType.CREDIT);
    }

    @Test
    void transactionType_fromCode_nulo_retornaNull() {
        assertThat(TransactionType.fromCode(null)).isNull();
    }

    @Test
    void transactionType_fromCode_invalido_lancaIllegalArgumentException() {
        assertThatThrownBy(() -> TransactionType.fromCode(99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    // ── TransactionType.fromValue ────────────────────────────────────────────

    @Test
    void transactionType_fromValue_debit_retornaDebit() {
        assertThat(TransactionType.fromValue("debit")).isEqualTo(TransactionType.DEBIT);
    }

    @Test
    void transactionType_fromValue_credit_retornaCredit() {
        assertThat(TransactionType.fromValue("credit")).isEqualTo(TransactionType.CREDIT);
    }

    @Test
    void transactionType_fromValue_nulo_lancaIllegalArgumentException() {
        assertThatThrownBy(() -> TransactionType.fromValue(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void transactionType_fromValue_invalido_lancaIllegalArgumentException() {
        assertThatThrownBy(() -> TransactionType.fromValue("transferencia"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("transferencia");
    }

    @Test
    void transactionType_getValue_retornaStringCorreta() {
        assertThat(TransactionType.DEBIT.getValue()).isEqualTo("debit");
        assertThat(TransactionType.CREDIT.getValue()).isEqualTo("credit");
    }

    @Test
    void transactionType_getCode_retornaCodigoCorreto() {
        assertThat(TransactionType.DEBIT.getCode()).isEqualTo(1);
        assertThat(TransactionType.CREDIT.getCode()).isEqualTo(2);
    }

    // ── GoalType.fromValue ───────────────────────────────────────────────────

    @Test
    void goalType_fromValue_expense_limit_retornaExpenseLimit() {
        assertThat(GoalType.fromValue("expense_limit")).isEqualTo(GoalType.EXPENSE_LIMIT);
    }

    @Test
    void goalType_fromValue_savings_retornaSavings() {
        assertThat(GoalType.fromValue("savings")).isEqualTo(GoalType.SAVINGS);
    }

    @Test
    void goalType_fromValue_income_retornaIncome() {
        assertThat(GoalType.fromValue("income")).isEqualTo(GoalType.INCOME);
    }

    @Test
    void goalType_fromValue_invalido_lancaIllegalArgumentException() {
        assertThatThrownBy(() -> GoalType.fromValue("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid");
    }

    @Test
    void goalType_getValue_retornaStringCorreta() {
        assertThat(GoalType.EXPENSE_LIMIT.getValue()).isEqualTo("expense_limit");
        assertThat(GoalType.SAVINGS.getValue()).isEqualTo("savings");
        assertThat(GoalType.INCOME.getValue()).isEqualTo("income");
    }

    // ── GoalStatus.fromValue ─────────────────────────────────────────────────

    @Test
    void goalStatus_fromValue_active_retornaActive() {
        assertThat(GoalStatus.fromValue("active")).isEqualTo(GoalStatus.ACTIVE);
    }

    @Test
    void goalStatus_fromValue_completed_retornaCompleted() {
        assertThat(GoalStatus.fromValue("completed")).isEqualTo(GoalStatus.COMPLETED);
    }

    @Test
    void goalStatus_fromValue_expired_retornaExpired() {
        assertThat(GoalStatus.fromValue("expired")).isEqualTo(GoalStatus.EXPIRED);
    }

    @Test
    void goalStatus_fromValue_archived_retornaArchived() {
        assertThat(GoalStatus.fromValue("archived")).isEqualTo(GoalStatus.ARCHIVED);
    }

    @Test
    void goalStatus_fromValue_invalido_lancaIllegalArgumentException() {
        assertThatThrownBy(() -> GoalStatus.fromValue("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown");
    }

    @Test
    void goalStatus_getValue_retornaStringCorreta() {
        assertThat(GoalStatus.ACTIVE.getValue()).isEqualTo("active");
        assertThat(GoalStatus.COMPLETED.getValue()).isEqualTo("completed");
        assertThat(GoalStatus.EXPIRED.getValue()).isEqualTo("expired");
        assertThat(GoalStatus.ARCHIVED.getValue()).isEqualTo("archived");
    }

    // ── FeedbackType values ───────────────────────────────────────────────────

    @Test
    void feedbackType_valoresEsperados() {
        assertThat(FeedbackType.values()).containsExactlyInAnyOrder(
                FeedbackType.SUGGESTION, FeedbackType.BUG, FeedbackType.GENERAL);
    }

    @Test
    void feedbackType_valueOf_suggestion() {
        assertThat(FeedbackType.valueOf("SUGGESTION")).isEqualTo(FeedbackType.SUGGESTION);
    }

    @Test
    void feedbackType_valueOf_bug() {
        assertThat(FeedbackType.valueOf("BUG")).isEqualTo(FeedbackType.BUG);
    }

    @Test
    void feedbackType_valueOf_general() {
        assertThat(FeedbackType.valueOf("GENERAL")).isEqualTo(FeedbackType.GENERAL);
    }

    @Test
    void feedbackType_valueOf_invalido_lancaIllegalArgumentException() {
        assertThatThrownBy(() -> FeedbackType.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
