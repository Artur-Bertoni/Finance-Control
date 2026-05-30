package com.financecontrol.service;

import com.financecontrol.dto.response.DashboardResponse;
import com.financecontrol.enums.TransactionType;
import com.financecontrol.repository.AccountRepository;
import com.financecontrol.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock AccountRepository     accountRepository;

    @InjectMocks ReportService reportService;

    /** Typed empty list helper — avoids List.of() inference to List<Object> */
    private static List<Object[]> noRows() {
        return new ArrayList<>();
    }

    private static List<Object[]> rows(Object[]... items) {
        List<Object[]> list = new ArrayList<>();
        for (Object[] item : items) list.add(item);
        return list;
    }

    // ── getDashboard – caso básico sem dados ─────────────────────────────────

    @Test
    void getDashboard_semDados_retornaEstruturaVazia() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end   = LocalDate.of(2025, 3, 31);

        when(transactionRepository.findMonthlyTotals(eq(1L), any(), any(), isNull()))
                .thenReturn(noRows());
        when(transactionRepository.findCategoryTotals(eq(1L), any(), any(), isNull()))
                .thenReturn(noRows());
        when(accountRepository.sumBalance(1L, null)).thenReturn(0.0);

        DashboardResponse result = reportService.getDashboard(1L, start, end, null);

        assertThat(result.monthlyData()).isNotEmpty(); // months filled even with no rows
        assertThat(result.categoryExpenses()).isEmpty();
        assertThat(result.categoryIncomes()).isEmpty();
        assertThat(result.balanceEvolution()).isNotEmpty();
    }

    // ── getDashboard – dados mensais com CREDIT e DEBIT ──────────────────────

    @Test
    void getDashboard_comLancamentos_preencheMonthlyData() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end   = LocalDate.of(2025, 1, 31);

        // row: [year, month, type, sum]
        Object[] creditRow = {2025, 1, TransactionType.CREDIT, 1000.0};
        Object[] debitRow  = {2025, 1, TransactionType.DEBIT,  500.0};

        when(transactionRepository.findMonthlyTotals(eq(1L), any(), any(), isNull()))
                .thenReturn(rows(creditRow, debitRow));
        when(transactionRepository.findCategoryTotals(eq(1L), any(), any(), isNull()))
                .thenReturn(noRows());
        when(accountRepository.sumBalance(1L, null)).thenReturn(1500.0);

        DashboardResponse result = reportService.getDashboard(1L, start, end, null);

        assertThat(result.monthlyData()).hasSize(1);
        DashboardResponse.MonthlyDataPoint pt = result.monthlyData().get(0);
        assertThat(pt.month()).isEqualTo("2025-01");
        assertThat(pt.income()).isEqualTo(1000.0);
        assertThat(pt.expenses()).isEqualTo(500.0);
    }

    // ── getDashboard – dados de categoria ────────────────────────────────────

    @Test
    void getDashboard_comCategoria_preencheCategoryExpensesEIncomes() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end   = LocalDate.of(2025, 1, 31);

        // row: [catId, catName, iconKey, type, sum]
        Object[] expenseRow = {10L, "Alimentação", "ph-fork", TransactionType.DEBIT, 300.0};
        Object[] incomeRow  = {11L, "Salário",     "ph-coin", TransactionType.CREDIT, 5000.0};

        when(transactionRepository.findMonthlyTotals(eq(1L), any(), any(), isNull()))
                .thenReturn(noRows());
        when(transactionRepository.findCategoryTotals(eq(1L), any(), any(), isNull()))
                .thenReturn(rows(expenseRow, incomeRow));
        when(accountRepository.sumBalance(1L, null)).thenReturn(5000.0);

        DashboardResponse result = reportService.getDashboard(1L, start, end, null);

        assertThat(result.categoryExpenses()).hasSize(1);
        assertThat(result.categoryExpenses().get(0).categoryName()).isEqualTo("Alimentação");
        assertThat(result.categoryIncomes()).hasSize(1);
        assertThat(result.categoryIncomes().get(0).categoryName()).isEqualTo("Salário");
    }

    // ── getDashboard – balanceEvolution retrocede meses ──────────────────────

    @Test
    void getDashboard_balanceEvolution_retrocedeMesesAteStart() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end   = LocalDate.of(2025, 3, 31);

        when(transactionRepository.findMonthlyTotals(eq(1L), any(), any(), isNull()))
                .thenReturn(noRows());
        when(transactionRepository.findCategoryTotals(eq(1L), any(), any(), isNull()))
                .thenReturn(noRows());
        when(accountRepository.sumBalance(1L, null)).thenReturn(900.0);

        DashboardResponse result = reportService.getDashboard(1L, start, end, null);

        assertThat(result.balanceEvolution()).isNotEmpty();
        assertThat(result.balanceEvolution().get(0).month()).isLessThanOrEqualTo("2025-01");
    }

    // ── getDashboard – tipo como código numérico (nativeQuery) ───────────────

    @Test
    void getDashboard_tipoComoNumero_tratadoCorretamente() {
        LocalDate start = LocalDate.of(2025, 2, 1);
        LocalDate end   = LocalDate.of(2025, 2, 28);

        // type as Integer code (simulates native query result)
        Object[] row = {2025, 2, 1 /* DEBIT code */, 200.0};

        when(transactionRepository.findMonthlyTotals(eq(1L), any(), any(), isNull()))
                .thenReturn(rows(row));
        when(transactionRepository.findCategoryTotals(eq(1L), any(), any(), isNull()))
                .thenReturn(noRows());
        when(accountRepository.sumBalance(1L, null)).thenReturn(200.0);

        DashboardResponse result = reportService.getDashboard(1L, start, end, null);

        assertThat(result.monthlyData()).hasSize(1);
        assertThat(result.monthlyData().get(0).expenses()).isEqualTo(200.0);
    }

    // ── getDashboard – com accountId filtrado ────────────────────────────────

    @Test
    void getDashboard_comAccountId_passaFiltroParaRepositorios() {
        LocalDate start = LocalDate.of(2025, 4, 1);
        LocalDate end   = LocalDate.of(2025, 4, 30);

        when(transactionRepository.findMonthlyTotals(eq(1L), any(), any(), eq(5L)))
                .thenReturn(noRows());
        when(transactionRepository.findCategoryTotals(eq(1L), any(), any(), eq(5L)))
                .thenReturn(noRows());
        when(accountRepository.sumBalance(1L, null)).thenReturn(0.0);

        reportService.getDashboard(1L, start, end, 5L);

        verify(transactionRepository).findMonthlyTotals(eq(1L), any(), any(), eq(5L));
        verify(transactionRepository).findCategoryTotals(eq(1L), any(), any(), eq(5L));
    }

    // ── getDashboard – startDate no futuro usa fallback para wealthStart ──────

    @Test
    void getDashboard_startDateNoFuturo_usaFallbackParaWealthStart() {
        LocalDate futureStart = LocalDate.now().plusYears(1);
        LocalDate futureEnd   = futureStart.plusMonths(1);

        when(transactionRepository.findMonthlyTotals(eq(1L), any(), any(), isNull()))
                .thenReturn(noRows());
        when(transactionRepository.findCategoryTotals(eq(1L), any(), any(), isNull()))
                .thenReturn(noRows());
        when(accountRepository.sumBalance(1L, null)).thenReturn(0.0);

        assertThatCode(() -> reportService.getDashboard(1L, futureStart, futureEnd, null))
                .doesNotThrowAnyException();
    }
}
