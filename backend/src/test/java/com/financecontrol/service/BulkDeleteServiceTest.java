package com.financecontrol.service;

import com.financecontrol.dto.response.BulkDeletePreviewResponse;
import com.financecontrol.dto.response.BulkDeleteResponse;
import com.financecontrol.entity.*;
import com.financecontrol.enums.BulkDeleteType;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkDeleteServiceTest {

    @Mock TransactionRepository          transactionRepository;
    @Mock AccountRepository              accountRepository;
    @Mock CategoryRepository             categoryRepository;
    @Mock TransactionLocaleRepository    transactionLocaleRepository;
    @Mock FinancialInstitutionRepository financialInstitutionRepository;
    @Mock GoalRepository                 goalRepository;
    @Mock BudgetRepository               budgetRepository;

    @Mock TransactionService          transactionService;
    @Mock AccountService              accountService;
    @Mock CategoryService             categoryService;
    @Mock TransactionLocaleService    transactionLocaleService;
    @Mock FinancialInstitutionService financialInstitutionService;
    @Mock GoalService                 goalService;
    @Mock BudgetService               budgetService;

    @InjectMocks BulkDeleteService bulkDeleteService;

    private static Category category(Long id, Long userId) {
        Category c = new Category();
        c.setId(id);
        c.setUserId(userId);
        return c;
    }

    private static Account account(Long id, Long userId) {
        Account a = new Account();
        a.setId(id);
        a.setUserId(userId);
        return a;
    }

    private static Transaction transaction(Long id, Long userId) {
        Transaction t = new Transaction();
        t.setId(id);
        t.setUserId(userId);
        return t;
    }

    @Test
    void delete_removeApenasItensDoUsuario() {
        when(categoryRepository.findAllById(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(category(1L, 7L), category(2L, 99L), category(3L, 7L)));

        BulkDeleteResponse result = bulkDeleteService.delete(BulkDeleteType.CATEGORIES, List.of(1L, 2L, 3L), 7L);

        assertThat(result.requested()).isEqualTo(3);
        assertThat(result.deleted()).isEqualTo(2);
        assertThat(result.skipped()).isEqualTo(1);
        verify(categoryService).delete(1L, 7L);
        verify(categoryService).delete(3L, 7L);
        verify(categoryService, never()).delete(2L, 7L);
    }

    @Test
    void delete_transacoesIgnoraAsQueJaForamRemovidasEmCascata() {
        when(transactionRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(transaction(1L, 7L), transaction(2L, 7L)));
        when(transactionRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.existsById(2L)).thenReturn(false);

        BulkDeleteResponse result = bulkDeleteService.delete(BulkDeleteType.TRANSACTIONS, List.of(1L, 2L), 7L);

        assertThat(result.deleted()).isEqualTo(1);
        assertThat(result.skipped()).isEqualTo(1);
        verify(transactionService).delete(1L, 7L);
        verify(transactionService, never()).delete(2L, 7L);
    }

    @Test
    void delete_locaisDelegaAoServicoQueDesvincula() {
        TransactionLocale locale = new TransactionLocale();
        locale.setId(2L);
        locale.setUserId(7L);
        when(transactionLocaleRepository.findAllById(List.of(2L))).thenReturn(List.of(locale));

        BulkDeleteResponse result = bulkDeleteService.delete(BulkDeleteType.TRANSACTION_LOCALES, List.of(2L), 7L);

        assertThat(result.deleted()).isEqualTo(1);
        verify(transactionLocaleService).delete(2L, 7L);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void delete_listaVaziaNaoChamaServicos() {
        BulkDeleteResponse result = bulkDeleteService.delete(BulkDeleteType.GOALS, List.of(), 7L);

        assertThat(result.deleted()).isZero();
        verifyNoInteractions(goalService);
    }

    @Test
    void delete_acimaDoLimiteLancaBusinessException() {
        List<Long> ids = LongStream.rangeClosed(1, 501).boxed().toList();

        assertThatThrownBy(() -> bulkDeleteService.delete(BulkDeleteType.CATEGORIES, ids, 7L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.bulkDelete.tooManyItems");
    }

    @Test
    void delete_orcamentosUsaAssinaturaComUserIdPrimeiro() {
        Budget b = new Budget();
        b.setId(4L);
        b.setUserId(7L);
        when(budgetRepository.findAllById(List.of(4L))).thenReturn(List.of(b));

        bulkDeleteService.delete(BulkDeleteType.BUDGETS, List.of(4L), 7L);

        verify(budgetService).delete(7L, 4L);
    }

    @Test
    void preview_categoriasContaTransacoesQueSeraoExcluidas() {
        when(categoryRepository.findAllById(List.of(1L))).thenReturn(List.of(category(1L, 7L)));
        when(transactionRepository.countByCategoryIdIn(List.of(1L))).thenReturn(12L);

        BulkDeletePreviewResponse result = bulkDeleteService.preview(BulkDeleteType.CATEGORIES, List.of(1L), 7L);

        assertThat(result.items()).isEqualTo(1);
        assertThat(result.deletedTransactions()).isEqualTo(12);
        assertThat(result.unlinkedTransactions()).isZero();
    }

    @Test
    void preview_locaisContaTransacoesQueSeraoApenasDesvinculadas() {
        TransactionLocale locale = new TransactionLocale();
        locale.setId(2L);
        locale.setUserId(7L);
        when(transactionLocaleRepository.findAllById(List.of(2L))).thenReturn(List.of(locale));
        when(transactionRepository.countByTransactionLocaleIdIn(List.of(2L))).thenReturn(8L);

        BulkDeletePreviewResponse result = bulkDeleteService.preview(BulkDeleteType.TRANSACTION_LOCALES, List.of(2L), 7L);

        assertThat(result.items()).isEqualTo(1);
        assertThat(result.unlinkedTransactions()).isEqualTo(8);
        assertThat(result.deletedTransactions()).isZero();
    }

    @Test
    void preview_instituicoesContaContasQueSeraoApenasDesvinculadas() {
        FinancialInstitution fi = new FinancialInstitution();
        fi.setId(3L);
        fi.setUserId(7L);
        when(financialInstitutionRepository.findAllById(List.of(3L))).thenReturn(List.of(fi));
        when(accountRepository.findIdsByFinancialInstitutionIdIn(List.of(3L))).thenReturn(List.of(10L, 11L));

        BulkDeletePreviewResponse result = bulkDeleteService.preview(BulkDeleteType.FINANCIAL_INSTITUTIONS, List.of(3L), 7L);

        assertThat(result.items()).isEqualTo(1);
        assertThat(result.unlinkedAccounts()).isEqualTo(2);
        assertThat(result.deletedTransactions()).isZero();
    }

    @Test
    void preview_transacoesExpandeParcelasEContrapartidaDeTransferencia() {
        Transaction parcela = transaction(1L, 7L);
        parcela.setInstallmentGroupId(50L);
        Transaction transferencia = transaction(2L, 7L);
        transferencia.setTransferPartnerId(9L);

        when(transactionRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(parcela, transferencia));
        when(transactionRepository.findIdsByInstallmentGroupIdIn(List.of(50L))).thenReturn(List.of(1L, 3L, 4L));

        BulkDeletePreviewResponse result = bulkDeleteService.preview(BulkDeleteType.TRANSACTIONS, List.of(1L, 2L), 7L);

        assertThat(result.items()).isEqualTo(2);
        assertThat(result.deletedTransactions()).isEqualTo(5);
    }

    @Test
    void preview_semItensDoUsuarioRetornaZeros() {
        when(accountRepository.findAllById(List.of(1L))).thenReturn(List.of(account(1L, 99L)));

        BulkDeletePreviewResponse result = bulkDeleteService.preview(BulkDeleteType.ACCOUNTS, List.of(1L), 7L);

        assertThat(result.items()).isZero();
        assertThat(result.deletedTransactions()).isZero();
        verifyNoInteractions(transactionRepository);
    }
}
