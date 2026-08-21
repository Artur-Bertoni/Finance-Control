package com.financecontrol.service;

import com.financecontrol.dto.response.BulkDeletePreviewResponse;
import com.financecontrol.dto.response.BulkDeleteResponse;
import com.financecontrol.entity.Transaction;
import com.financecontrol.enums.BulkEntityType;
import com.financecontrol.repository.AccountRepository;
import com.financecontrol.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkDeleteServiceTest {

    @Mock BulkOwnershipResolver ownershipResolver;
    @Mock TransactionRepository transactionRepository;
    @Mock AccountRepository     accountRepository;

    @Mock TransactionService          transactionService;
    @Mock AccountService              accountService;
    @Mock CategoryService             categoryService;
    @Mock TransactionLocaleService    transactionLocaleService;
    @Mock FinancialInstitutionService financialInstitutionService;
    @Mock GoalService                 goalService;
    @Mock BudgetService               budgetService;

    @InjectMocks BulkDeleteService bulkDeleteService;

    private static Transaction transaction(Long id, Long userId) {
        Transaction t = new Transaction();
        t.setId(id);
        t.setUserId(userId);
        return t;
    }

    @Test
    void delete_removeSomenteOsIdsResolvidosComoDoUsuario() {
        when(ownershipResolver.ownedIds(BulkEntityType.CATEGORIES, List.of(1L, 2L, 3L), 7L))
                .thenReturn(List.of(1L, 3L));

        BulkDeleteResponse result = bulkDeleteService.delete(BulkEntityType.CATEGORIES, List.of(1L, 2L, 3L), 7L);

        assertThat(result.requested()).isEqualTo(3);
        assertThat(result.deleted()).isEqualTo(2);
        assertThat(result.skipped()).isEqualTo(1);
        verify(categoryService).delete(1L, 7L);
        verify(categoryService).delete(3L, 7L);
        verify(categoryService, never()).delete(2L, 7L);
    }

    @Test
    void delete_transacoesIgnoraAsQueJaForamRemovidasEmCascata() {
        when(ownershipResolver.ownedIds(BulkEntityType.TRANSACTIONS, List.of(1L, 2L), 7L))
                .thenReturn(List.of(1L, 2L));
        when(transactionRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.existsById(2L)).thenReturn(false);

        BulkDeleteResponse result = bulkDeleteService.delete(BulkEntityType.TRANSACTIONS, List.of(1L, 2L), 7L);

        assertThat(result.deleted()).isEqualTo(1);
        assertThat(result.skipped()).isEqualTo(1);
        verify(transactionService).delete(1L, 7L);
        verify(transactionService, never()).delete(2L, 7L);
    }

    @Test
    void delete_locaisDelegaAoServicoQueDesvincula() {
        when(ownershipResolver.ownedIds(BulkEntityType.TRANSACTION_LOCALES, List.of(2L), 7L))
                .thenReturn(List.of(2L));

        BulkDeleteResponse result = bulkDeleteService.delete(BulkEntityType.TRANSACTION_LOCALES, List.of(2L), 7L);

        assertThat(result.deleted()).isEqualTo(1);
        verify(transactionLocaleService).delete(2L, 7L);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void delete_listaVaziaNaoChamaServicos() {
        when(ownershipResolver.ownedIds(BulkEntityType.GOALS, List.of(), 7L)).thenReturn(List.of());

        BulkDeleteResponse result = bulkDeleteService.delete(BulkEntityType.GOALS, List.of(), 7L);

        assertThat(result.deleted()).isZero();
        verifyNoInteractions(goalService);
    }

    @Test
    void delete_orcamentosUsaAssinaturaComUserIdPrimeiro() {
        when(ownershipResolver.ownedIds(BulkEntityType.BUDGETS, List.of(4L), 7L)).thenReturn(List.of(4L));

        bulkDeleteService.delete(BulkEntityType.BUDGETS, List.of(4L), 7L);

        verify(budgetService).delete(7L, 4L);
    }

    @Test
    void preview_categoriasContaTransacoesQueSeraoExcluidas() {
        when(ownershipResolver.ownedIds(BulkEntityType.CATEGORIES, List.of(1L), 7L)).thenReturn(List.of(1L));
        when(transactionRepository.countByCategoryIdIn(List.of(1L))).thenReturn(12L);

        BulkDeletePreviewResponse result = bulkDeleteService.preview(BulkEntityType.CATEGORIES, List.of(1L), 7L);

        assertThat(result.items()).isEqualTo(1);
        assertThat(result.deletedTransactions()).isEqualTo(12);
        assertThat(result.unlinkedTransactions()).isZero();
    }

    @Test
    void preview_locaisContaTransacoesQueSeraoApenasDesvinculadas() {
        when(ownershipResolver.ownedIds(BulkEntityType.TRANSACTION_LOCALES, List.of(2L), 7L)).thenReturn(List.of(2L));
        when(transactionRepository.countByTransactionLocaleIdIn(List.of(2L))).thenReturn(8L);

        BulkDeletePreviewResponse result = bulkDeleteService.preview(BulkEntityType.TRANSACTION_LOCALES, List.of(2L), 7L);

        assertThat(result.items()).isEqualTo(1);
        assertThat(result.unlinkedTransactions()).isEqualTo(8);
        assertThat(result.deletedTransactions()).isZero();
    }

    @Test
    void preview_instituicoesContaContasQueSeraoApenasDesvinculadas() {
        when(ownershipResolver.ownedIds(BulkEntityType.FINANCIAL_INSTITUTIONS, List.of(3L), 7L)).thenReturn(List.of(3L));
        when(accountRepository.findIdsByFinancialInstitutionIdIn(List.of(3L))).thenReturn(List.of(10L, 11L));

        BulkDeletePreviewResponse result = bulkDeleteService.preview(BulkEntityType.FINANCIAL_INSTITUTIONS, List.of(3L), 7L);

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

        when(ownershipResolver.ownedIds(BulkEntityType.TRANSACTIONS, List.of(1L, 2L), 7L)).thenReturn(List.of(1L, 2L));
        when(transactionRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(parcela, transferencia));
        when(transactionRepository.findIdsByInstallmentGroupIdIn(List.of(50L))).thenReturn(List.of(1L, 3L, 4L));

        BulkDeletePreviewResponse result = bulkDeleteService.preview(BulkEntityType.TRANSACTIONS, List.of(1L, 2L), 7L);

        assertThat(result.items()).isEqualTo(2);
        assertThat(result.deletedTransactions()).isEqualTo(5);
    }

    @Test
    void preview_semItensDoUsuarioRetornaZeros() {
        when(ownershipResolver.ownedIds(BulkEntityType.ACCOUNTS, List.of(1L), 7L)).thenReturn(List.of());

        BulkDeletePreviewResponse result = bulkDeleteService.preview(BulkEntityType.ACCOUNTS, List.of(1L), 7L);

        assertThat(result.items()).isZero();
        assertThat(result.deletedTransactions()).isZero();
        verifyNoInteractions(transactionRepository);
    }
}
