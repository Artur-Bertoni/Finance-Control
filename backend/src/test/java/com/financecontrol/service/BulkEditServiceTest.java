package com.financecontrol.service;

import com.financecontrol.dto.request.*;
import com.financecontrol.dto.response.BulkEditResponse;
import com.financecontrol.entity.*;
import com.financecontrol.enums.AccountType;
import com.financecontrol.enums.BulkEntityType;
import com.financecontrol.enums.TransactionType;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkEditServiceTest {

    @Mock BulkOwnershipResolver          ownershipResolver;
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

    @InjectMocks BulkEditService bulkEditService;

    private static BulkEditValues values() {
        return new BulkEditValues(null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private static Transaction transaction(Long id, Long userId) {
        Transaction t = new Transaction();
        t.setId(id);
        t.setUserId(userId);
        t.setValue(50.0);
        t.setDate(LocalDate.of(2026, 3, 10));
        t.setType(TransactionType.DEBIT);
        t.setAccount(account(1L, userId));
        t.setCategory(category(2L, userId));
        t.setObs("obs original");
        return t;
    }

    private static Account account(Long id, Long userId) {
        Account a = new Account();
        a.setId(id);
        a.setUserId(userId);
        a.setName("Conta");
        a.setType(AccountType.CHECKING);
        a.setBalance(100.0);
        return a;
    }

    private static Category category(Long id, Long userId) {
        Category c = new Category();
        c.setId(id);
        c.setUserId(userId);
        c.setName("Categoria");
        return c;
    }

    @Test
    void edit_semCamposLancaBusinessException() {
        assertThatThrownBy(() -> bulkEditService.edit(BulkEntityType.CATEGORIES, List.of(1L), Set.of(), values(), 7L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.bulkEdit.noFields");
    }

    @Test
    void edit_campoNaoPermitidoParaOTipoLancaBusinessException() {
        assertThatThrownBy(() -> bulkEditService.edit(BulkEntityType.CATEGORIES, List.of(1L), Set.of("value"), values(), 7L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.bulkEdit.invalidField");
    }

    @Test
    void edit_categoriaAlteraSomenteOCampoMarcadoEPreservaOResto() {
        Category c = category(1L, 7L);
        c.setDescription("descricao antiga");
        c.setIconKey("ph-tag");
        when(ownershipResolver.ownedIds(BulkEntityType.CATEGORIES, List.of(1L), 7L)).thenReturn(List.of(1L));
        when(categoryRepository.findAllById(List.of(1L))).thenReturn(List.of(c));

        BulkEditValues v = new BulkEditValues(null, null, null, null, null, null, null, null, "nova descricao",
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        BulkEditResponse result = bulkEditService.edit(BulkEntityType.CATEGORIES, List.of(1L), Set.of("description"), v, 7L);

        ArgumentCaptor<CategoryRequest> captor = ArgumentCaptor.forClass(CategoryRequest.class);
        verify(categoryService).update(eq(1L), eq(7L), captor.capture());

        assertThat(captor.getValue().description()).isEqualTo("nova descricao");
        assertThat(captor.getValue().name()).isEqualTo("Categoria");
        assertThat(captor.getValue().iconKey()).isEqualTo("ph-tag");
        assertThat(result.edited()).isEqualTo(1);
    }

    @Test
    void edit_campoMarcadoComValorNuloLimpaOCampo() {
        TransactionLocale l = new TransactionLocale();
        l.setId(3L);
        l.setUserId(7L);
        l.setName("Local");
        l.setAddress("Rua Antiga");
        when(ownershipResolver.ownedIds(BulkEntityType.TRANSACTION_LOCALES, List.of(3L), 7L)).thenReturn(List.of(3L));
        when(transactionLocaleRepository.findAllById(List.of(3L))).thenReturn(List.of(l));

        bulkEditService.edit(BulkEntityType.TRANSACTION_LOCALES, List.of(3L), Set.of("address"), values(), 7L);

        ArgumentCaptor<TransactionLocaleRequest> captor = ArgumentCaptor.forClass(TransactionLocaleRequest.class);
        verify(transactionLocaleService).update(eq(3L), eq(7L), captor.capture());

        assertThat(captor.getValue().address()).isNull();
        assertThat(captor.getValue().name()).isEqualTo("Local");
    }

    @Test
    void edit_transacaoSimplesTrocaCategoriaEMantemValorEData() {
        Transaction t = transaction(10L, 7L);
        when(ownershipResolver.ownedIds(BulkEntityType.TRANSACTIONS, List.of(10L), 7L)).thenReturn(List.of(10L));
        when(transactionRepository.findAllById(List.of(10L))).thenReturn(List.of(t));
        when(transactionRepository.existsById(10L)).thenReturn(true);

        BulkEditValues v = new BulkEditValues(null, 99L, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null);

        bulkEditService.edit(BulkEntityType.TRANSACTIONS, List.of(10L), Set.of("categoryId"), v, 7L);

        ArgumentCaptor<TransactionRequest> captor = ArgumentCaptor.forClass(TransactionRequest.class);
        verify(transactionService).update(eq(10L), eq(7L), captor.capture());

        assertThat(captor.getValue().categoryId()).isEqualTo(99L);
        assertThat(captor.getValue().value()).isEqualTo(50.0);
        assertThat(captor.getValue().date()).isEqualTo(LocalDate.of(2026, 3, 10));
        assertThat(captor.getValue().obs()).isEqualTo("obs original");
    }

    @Test
    void edit_parcelamentoUsaValorTotalDoGrupoEDataDaPrimeiraParcela() {
        Transaction parcela2 = transaction(11L, 7L);
        parcela2.setInstallmentGroupId(10L);
        parcela2.setInstallmentIndex(2);
        parcela2.setValue(50.0);
        parcela2.setDate(LocalDate.of(2026, 4, 10));

        Transaction parcela1 = transaction(10L, 7L);
        parcela1.setInstallmentGroupId(10L);
        parcela1.setInstallmentIndex(1);
        parcela1.setValue(50.0);
        parcela1.setDate(LocalDate.of(2026, 3, 10));

        when(ownershipResolver.ownedIds(BulkEntityType.TRANSACTIONS, List.of(11L), 7L)).thenReturn(List.of(11L));
        when(transactionRepository.findAllById(List.of(11L))).thenReturn(List.of(parcela2));
        when(transactionRepository.existsById(11L)).thenReturn(true);
        when(transactionRepository.findByInstallmentGroupIdOrderByInstallmentIndexAsc(10L))
                .thenReturn(List.of(parcela1, parcela2));

        BulkEditValues v = new BulkEditValues(null, 99L, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null);

        bulkEditService.edit(BulkEntityType.TRANSACTIONS, List.of(11L), Set.of("categoryId"), v, 7L);

        ArgumentCaptor<TransactionRequest> captor = ArgumentCaptor.forClass(TransactionRequest.class);
        verify(transactionService).update(eq(11L), eq(7L), captor.capture());

        assertThat(captor.getValue().value()).isEqualTo(100.0);
        assertThat(captor.getValue().date()).isEqualTo(LocalDate.of(2026, 3, 10));
        assertThat(captor.getValue().installmentsNumber()).isEqualTo(2);
    }

    @Test
    void edit_contaSemInstituicaoMantemVinculoVazioAoAlterarOutroCampo() {
        Account a = account(4L, 7L);
        a.setFinancialInstitution(null);
        when(ownershipResolver.ownedIds(BulkEntityType.ACCOUNTS, List.of(4L), 7L)).thenReturn(List.of(4L));
        when(accountRepository.findAllById(List.of(4L))).thenReturn(List.of(a));

        BulkEditValues v = new BulkEditValues(null, null, null, null, null, null, null, null, null, null, null,
                "ph-wallet", null, null, null, null, null, null, null, null, null, null, null, null);

        bulkEditService.edit(BulkEntityType.ACCOUNTS, List.of(4L), Set.of("iconKey"), v, 7L);

        ArgumentCaptor<AccountRequest> captor = ArgumentCaptor.forClass(AccountRequest.class);
        verify(accountService).update(eq(4L), eq(7L), captor.capture());

        assertThat(captor.getValue().financialInstitutionId()).isNull();
        assertThat(captor.getValue().iconKey()).isEqualTo("ph-wallet");
        assertThat(captor.getValue().balance()).isEqualTo(100.0);
    }

    @Test
    void edit_orcamentoAtualizaLimitePelaCategoria() {
        Budget b = new Budget();
        b.setId(6L);
        b.setUserId(7L);
        b.setCategory(category(2L, 7L));
        when(ownershipResolver.ownedIds(BulkEntityType.BUDGETS, List.of(6L), 7L)).thenReturn(List.of(6L));
        when(budgetRepository.findAllById(List.of(6L))).thenReturn(List.of(b));

        BulkEditValues v = new BulkEditValues(null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, 350.0);

        BulkEditResponse result = bulkEditService.edit(BulkEntityType.BUDGETS, List.of(6L), Set.of("monthlyLimit"), v, 7L);

        verify(budgetService).upsert(7L, new BudgetRequest(2L, 350.0));
        assertThat(result.edited()).isEqualTo(1);
    }

    @Test
    void edit_reportaIdsQueNaoPertencemAoUsuarioComoIgnorados() {
        when(ownershipResolver.ownedIds(BulkEntityType.CATEGORIES, List.of(1L, 2L), 7L)).thenReturn(List.of());
        when(categoryRepository.findAllById(List.of())).thenReturn(List.of());

        BulkEditResponse result = bulkEditService.edit(BulkEntityType.CATEGORIES, List.of(1L, 2L), Set.of("iconKey"), values(), 7L);

        assertThat(result.requested()).isEqualTo(2);
        assertThat(result.edited()).isZero();
        assertThat(result.skipped()).isEqualTo(2);
        verifyNoInteractions(categoryService);
    }
}
