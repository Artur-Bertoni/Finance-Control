package com.financecontrol.service;

import com.financecontrol.dto.request.TransactionRequest;
import com.financecontrol.dto.response.TransactionResponse;
import com.financecontrol.entity.Account;
import com.financecontrol.entity.Category;
import com.financecontrol.entity.FinancialInstitution;
import com.financecontrol.entity.Transaction;
import com.financecontrol.entity.TransactionLocale;
import com.financecontrol.enums.TransactionType;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.AccountRepository;
import com.financecontrol.repository.CategoryRepository;
import com.financecontrol.repository.TransactionLocaleRepository;
import com.financecontrol.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"null", "unchecked"})
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock TransactionRepository    transactionRepository;
    @Mock AccountRepository        accountRepository;
    @Mock CategoryRepository       categoryRepository;
    @Mock TransactionLocaleRepository transactionLocaleRepository;
    @Mock HistoryService           historyService;
    @Mock AccountService           accountService;

    @InjectMocks TransactionService transactionService;

    // ------------------------------------------------------------------ helpers

    private static FinancialInstitution fi(Long id) {
        FinancialInstitution fi = new FinancialInstitution();
        fi.setId(id);
        fi.setName("Banco " + id);
        return fi;
    }

    private static Account account(Long id) {
        Account a = new Account();
        a.setId(id);
        a.setUserId(1L);
        a.setFinancialInstitution(fi(1L));
        a.setName("Conta " + id);
        a.setBalance(1000.0);
        return a;
    }

    private static Category category(Long id) {
        Category c = new Category();
        c.setId(id);
        c.setName("Cat " + id);
        return c;
    }

    private static Transaction transaction(Long id, Account acc, Category cat) {
        return new Transaction(id, 1L, acc, cat, null,
                100.0, LocalDate.of(2025, 1, 15), TransactionType.DEBIT,
                0, null, 0L, LocalDateTime.now(), null, null, null);
    }

    private static TransactionRequest request(Long accountId, Long categoryId) {
        return new TransactionRequest(accountId, categoryId, null, 100.0,
                LocalDate.of(2025, 1, 15), TransactionType.DEBIT, 0, null, null);
    }

    // ------------------------------------------------------------------ findAllByUser

    @Test
    void findAllByUser_retornaListaDoRepositorio() {
        Account acc = account(1L);
        Category cat = category(1L);
        Transaction tx = transaction(10L, acc, cat);

        when(transactionRepository.findAllFiltered(1L, LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31), null, null))
                .thenReturn(List.of(tx));

        List<TransactionResponse> result = transactionService.findAllByUser(
                1L, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(10L);
    }

    @Test
    void findAllByUser_filtroCategoria_repassa_parametro() {
        when(transactionRepository.findAllFiltered(any(), any(), any(), eq(5L), any()))
                .thenReturn(List.of());

        List<TransactionResponse> result = transactionService.findAllByUser(
                1L, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 5L, null);

        assertThat(result).isEmpty();
        verify(transactionRepository).findAllFiltered(1L,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 5L, null);
    }

    // ------------------------------------------------------------------ findById

    @Test
    void findById_encontrado_retornaResponse() {
        Account acc = account(1L);
        Category cat = category(1L);
        Transaction tx = transaction(10L, acc, cat);

        when(transactionRepository.findById(10L)).thenReturn(Optional.of(tx));

        TransactionResponse resp = transactionService.findById(10L);

        assertThat(resp.id()).isEqualTo(10L);
        assertThat(resp.value()).isEqualTo(100.0);
    }

    @Test
    void findById_naoEncontrado_lancaResourceNotFoundException() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ------------------------------------------------------------------ create

    @Test
    void create_sucesso_salvaERegistraHistorico() {
        Account acc = account(1L);
        Category cat = category(2L);
        TransactionRequest req = request(1L, 2L);

        when(transactionRepository.existsDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(false);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(20L);
            return t;
        });

        TransactionResponse resp = transactionService.create(1L, req, false);

        assertThat(resp.id()).isEqualTo(20L);
        verify(accountService).patchBalance(eq(1L), anyDouble());
        verify(historyService).recordCreation(HistoryService.ENTITY_TRANSACTION, 20L, 1L);
    }

    @Test
    void create_duplicataDetectada_lancaBusinessException() {
        TransactionRequest req = request(1L, 2L);

        when(transactionRepository.existsDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> transactionService.create(1L, req, false))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("duplicate");
    }

    @Test
    void create_comForce_ignoraDuplicataEsalva() {
        Account acc = account(1L);
        Category cat = category(2L);
        TransactionRequest req = request(1L, 2L);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(30L);
            return t;
        });

        TransactionResponse resp = transactionService.create(1L, req, true);

        assertThat(resp.id()).isEqualTo(30L);
        verify(transactionRepository, never()).existsDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void create_contaNaoEncontrada_lancaResourceNotFoundException() {
        TransactionRequest req = request(99L, 2L);

        when(transactionRepository.existsDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(false);
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.create(1L, req, false))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ------------------------------------------------------------------ update

    @Test
    void update_sucesso_atualizaERetornaResponse() {
        Account acc = account(1L);
        Category cat = category(2L);
        Transaction existing = transaction(10L, acc, cat);
        TransactionRequest req = request(1L, 2L);

        when(transactionRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse resp = transactionService.update(10L, 1L, req);

        assertThat(resp.id()).isEqualTo(10L);
        verify(accountService, atLeastOnce()).patchBalance(any(), anyDouble());
    }

    @Test
    void update_transacaoNaoEncontrada_lancaResourceNotFoundException() {
        TransactionRequest req = request(1L, 2L);
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.update(99L, 1L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ------------------------------------------------------------------ delete

    @Test
    void delete_sucesso_removeDoBanco() {
        Account acc = account(1L);
        Category cat = category(2L);
        Transaction tx = transaction(10L, acc, cat);

        when(transactionRepository.findById(10L)).thenReturn(Optional.of(tx));

        transactionService.delete(10L);

        verify(transactionRepository).deleteById(10L);
        verify(accountService).patchBalance(eq(1L), anyDouble());
    }

    @Test
    void delete_naoEncontrada_lancaResourceNotFoundException() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_comTransferPartner_removeParceiro() {
        Account acc = account(1L);
        Category cat = category(2L);

        Transaction partner = transaction(11L, acc, cat);
        partner.setTransferPartnerId(0L);

        Transaction tx = transaction(10L, acc, cat);
        tx.setTransferPartnerId(11L);

        when(transactionRepository.findById(10L)).thenReturn(Optional.of(tx));
        when(transactionRepository.findById(11L)).thenReturn(Optional.of(partner));
        when(transactionRepository.existsById(11L)).thenReturn(true);

        transactionService.delete(10L);

        verify(transactionRepository).deleteById(10L);
        verify(transactionRepository).deleteById(11L);
    }

    // ------------------------------------------------------------------ patchTransferPartner

    @Test
    void patchTransferPartner_atualizaParceiroId() {
        Account acc = account(1L);
        Category cat = category(2L);
        Transaction tx = transaction(10L, acc, cat);

        when(transactionRepository.findById(10L)).thenReturn(Optional.of(tx));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse resp = transactionService.patchTransferPartner(10L, 99L);

        assertThat(resp.transferPartnerId()).isEqualTo(99L);
        verify(transactionRepository).save(tx);
    }

    @Test
    void patchTransferPartner_naoEncontrado_lancaResourceNotFoundException() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.patchTransferPartner(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void patchTransferPartner_removeParceiro_setaNulo() {
        Account acc = account(1L);
        Category cat = category(2L);
        Transaction tx = transaction(10L, acc, cat);
        tx.setTransferPartnerId(5L);

        when(transactionRepository.findById(10L)).thenReturn(Optional.of(tx));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse resp = transactionService.patchTransferPartner(10L, null);

        assertThat(resp.transferPartnerId()).isNull();
    }

    // ------------------------------------------------------------------ new coverage

    private static TransactionLocale locale(Long id) {
        TransactionLocale l = new TransactionLocale();
        l.setId(id);
        l.setName("Local " + id);
        return l;
    }

    @Test
    void create_comLocale_carregaLocaleEAssociaTransacao() {
        Account acc = account(1L);
        Category cat = category(2L);
        TransactionLocale loc = locale(7L);
        TransactionRequest req = new TransactionRequest(1L, 2L, 7L, 100.0,
                LocalDate.of(2025, 1, 15), TransactionType.DEBIT, 0, null, null);

        when(transactionRepository.existsDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(false);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat));
        when(transactionLocaleRepository.findById(7L)).thenReturn(Optional.of(loc));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(40L);
            return t;
        });

        TransactionResponse resp = transactionService.create(1L, req, false);

        assertThat(resp.id()).isEqualTo(40L);
        verify(transactionLocaleRepository).findById(7L);
    }

    @Test
    void update_happyPath_reverteEAplicaSaldoRegistraDiffSalva() {
        Account oldAcc = account(1L);
        Account newAcc = account(2L);
        Category oldCat = category(3L);
        Category newCat = category(4L);

        Transaction existing = transaction(10L, oldAcc, oldCat);
        TransactionRequest req = new TransactionRequest(2L, 4L, null, 250.0,
                LocalDate.of(2025, 2, 20), TransactionType.CREDIT, 0, "nova obs", null);

        when(transactionRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(newAcc));
        when(categoryRepository.findById(4L)).thenReturn(Optional.of(newCat));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse resp = transactionService.update(10L, 1L, req);

        assertThat(resp.id()).isEqualTo(10L);
        assertThat(resp.value()).isEqualTo(250.0);
        assertThat(resp.type()).isEqualTo(TransactionType.CREDIT);

        verify(accountService).patchBalance(1L, 100.0);
        verify(accountService).patchBalance(2L, 250.0);

        ArgumentCaptor<Map<String, String[]>> diffCaptor = ArgumentCaptor.forClass(Map.class);
        verify(historyService).recordChanges(eq(HistoryService.ENTITY_TRANSACTION), eq(10L), eq(1L), diffCaptor.capture());
        assertThat(diffCaptor.getValue()).containsKeys("account", "category", "value", "date", "type", "obs");
    }

    @Test
    void update_comTransferPartnerValorDiferente_atualizaParceiroRecursivamente() {
        Account acc = account(1L);
        Category cat = category(2L);

        Transaction partner = transaction(11L, acc, cat);
        partner.setTransferPartnerId(0L);

        Transaction main = transaction(10L, acc, cat);
        main.setTransferPartnerId(11L);

        TransactionRequest req = new TransactionRequest(1L, 2L, null, 300.0,
                LocalDate.of(2025, 1, 15), TransactionType.DEBIT, 0, null, null);

        when(transactionRepository.findById(10L)).thenReturn(Optional.of(main));
        when(transactionRepository.findById(11L)).thenReturn(Optional.of(partner));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        transactionService.update(10L, 1L, req);

        assertThat(partner.getType()).isEqualTo(TransactionType.CREDIT);
        assertThat(partner.getValue()).isEqualTo(300.0);

        verify(transactionRepository, atLeast(2)).save(any(Transaction.class));
    }

    @Test
    void update_contaNaoEncontrada_lancaResourceNotFoundException() {
        Account acc = account(1L);
        Category cat = category(2L);
        Transaction existing = transaction(10L, acc, cat);
        TransactionRequest req = new TransactionRequest(99L, 2L, null, 100.0,
                LocalDate.of(2025, 1, 15), TransactionType.DEBIT, 0, null, null);

        when(transactionRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.update(10L, 1L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_categoriaNaoEncontrada_lancaResourceNotFoundException() {
        Account acc = account(1L);
        Category cat = category(2L);
        Transaction existing = transaction(10L, acc, cat);
        TransactionRequest req = new TransactionRequest(1L, 99L, null, 100.0,
                LocalDate.of(2025, 1, 15), TransactionType.DEBIT, 0, null, null);

        when(transactionRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.update(10L, 1L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ------------------------------------------------------------------ buildDiff per-field branches

    private Map<String, String[]> captureUpdateDiff(Transaction existing, TransactionRequest req) {
        Account acc = existing.getAccount();
        Category cat = existing.getCategory();
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(accountRepository.findById(req.accountId())).thenReturn(Optional.of(acc));
        when(categoryRepository.findById(req.categoryId())).thenReturn(Optional.of(cat));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        transactionService.update(10L, 1L, req);

        ArgumentCaptor<Map<String, String[]>> diff = ArgumentCaptor.forClass(Map.class);
        verify(historyService).recordChanges(eq(HistoryService.ENTITY_TRANSACTION), eq(10L), eq(1L), diff.capture());
        return diff.getValue();
    }

    @Test
    void update_mudaApenasData_registraDiffDate() {
        Transaction existing = transaction(10L, account(1L), category(2L));
        TransactionRequest req = new TransactionRequest(1L, 2L, null, 100.0,
                LocalDate.of(2025, 3, 1), TransactionType.DEBIT, 0, null, null);

        Map<String, String[]> diff = captureUpdateDiff(existing, req);

        assertThat(diff).containsOnlyKeys("date");
    }

    @Test
    void update_mudaApenasTipo_registraDiffType() {
        Transaction existing = transaction(10L, account(1L), category(2L));
        TransactionRequest req = new TransactionRequest(1L, 2L, null, 100.0,
                LocalDate.of(2025, 1, 15), TransactionType.CREDIT, 0, null, null);

        Map<String, String[]> diff = captureUpdateDiff(existing, req);

        assertThat(diff).containsOnlyKeys("type");
    }

    @Test
    void update_mudaApenasParcelas_registraDiffInstallmentsNumber() {
        Transaction existing = transaction(10L, account(1L), category(2L));
        TransactionRequest req = new TransactionRequest(1L, 2L, null, 100.0,
                LocalDate.of(2025, 1, 15), TransactionType.DEBIT, 3, null, null);

        Map<String, String[]> diff = captureUpdateDiff(existing, req);

        assertThat(diff).containsOnlyKeys("installmentsNumber");
    }

    @Test
    void update_mudaApenasObs_registraDiffObs() {
        Transaction existing = transaction(10L, account(1L), category(2L));
        TransactionRequest req = new TransactionRequest(1L, 2L, null, 100.0,
                LocalDate.of(2025, 1, 15), TransactionType.DEBIT, 0, "observacao", null);

        Map<String, String[]> diff = captureUpdateDiff(existing, req);

        assertThat(diff).containsOnlyKeys("obs");
    }

    @Test
    void update_mudaApenasLocale_registraDiffTransactionLocale() {
        Transaction existing = transaction(10L, account(1L), category(2L));
        TransactionLocale loc = locale(7L);
        TransactionRequest req = new TransactionRequest(1L, 2L, 7L, 100.0,
                LocalDate.of(2025, 1, 15), TransactionType.DEBIT, 0, null, null);

        when(transactionRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(existing.getAccount()));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(existing.getCategory()));
        when(transactionLocaleRepository.findById(7L)).thenReturn(Optional.of(loc));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        transactionService.update(10L, 1L, req);

        ArgumentCaptor<Map<String, String[]>> diff = ArgumentCaptor.forClass(Map.class);
        verify(historyService).recordChanges(eq(HistoryService.ENTITY_TRANSACTION), eq(10L), eq(1L), diff.capture());
        assertThat(diff.getValue()).containsOnlyKeys("transactionLocale");
    }

    // ------------------------------------------------------------------ parcelamento (installments)

    @Test
    void create_parcelado_geraUmaTransacaoPorParcelaLigadasPorGrupo() {
        Account acc = account(1L);
        Category cat = category(2L);
        TransactionRequest req = new TransactionRequest(1L, 2L, null, 100.0,
                LocalDate.of(2025, 1, 15), TransactionType.DEBIT, 3, null, null);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            if (t.getId() == null) t.setId(100L);
            return t;
        });

        TransactionResponse resp = transactionService.create(1L, req, false);

        assertThat(resp.installmentIndex()).isEqualTo(1);
        assertThat(resp.installmentGroupId()).isEqualTo(100L);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, atLeast(3)).save(captor.capture());

        Map<Integer, Double> byIndex = new HashMap<>();
        for (Transaction t : captor.getAllValues()) byIndex.put(t.getInstallmentIndex(), t.getValue());

        assertThat(byIndex.keySet()).containsExactlyInAnyOrder(1, 2, 3);
        assertThat(byIndex.get(1)).isCloseTo(33.34, within(0.001));
        assertThat(byIndex.get(2)).isCloseTo(33.33, within(0.001));
        assertThat(byIndex.get(3)).isCloseTo(33.33, within(0.001));
        assertThat(byIndex.values().stream().mapToDouble(Double::doubleValue).sum()).isCloseTo(100.0, within(0.001));

        verify(transactionRepository, never()).existsDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void create_parcelado_parcelasFuturasNaoEntramNoSaldoAgora() {
        Account acc = account(1L);
        Category cat = category(2L);
        TransactionRequest req = new TransactionRequest(1L, 2L, null, 90.0,
                LocalDate.now(), TransactionType.DEBIT, 3, null, null);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            if (t.getId() == null) t.setId(200L);
            return t;
        });

        transactionService.create(1L, req, false);

        verify(accountService, times(1)).patchBalance(1L, -30.0);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, atLeast(3)).save(captor.capture());
        Map<Integer, Boolean> appliedByIndex = new HashMap<>();
        for (Transaction t : captor.getAllValues()) appliedByIndex.put(t.getInstallmentIndex(), t.getApplied());
        assertThat(appliedByIndex.get(1)).isTrue();
        assertThat(appliedByIndex.get(2)).isFalse();
        assertThat(appliedByIndex.get(3)).isFalse();
    }

    @Test
    void delete_parcela_removeGrupoInteiroRevertendoSaldoDasAplicadas() {
        Account acc = account(1L);
        Category cat = category(2L);

        Transaction p1 = transaction(50L, acc, cat);
        p1.setInstallmentGroupId(50L); p1.setInstallmentIndex(1); p1.setApplied(true);
        Transaction p2 = transaction(51L, acc, cat);
        p2.setInstallmentGroupId(50L); p2.setInstallmentIndex(2); p2.setApplied(false);

        when(transactionRepository.findById(51L)).thenReturn(Optional.of(p2));
        when(transactionRepository.findByInstallmentGroupIdOrderByInstallmentIndexAsc(50L)).thenReturn(List.of(p1, p2));

        transactionService.delete(51L);

        verify(transactionRepository).deleteById(50L);
        verify(transactionRepository).deleteById(51L);
        verify(accountService, times(1)).patchBalance(1L, 100.0);
    }

    @Test
    void update_parcelado_editaEmCascataReDividindoValorEnumeroDeParcelas() {
        Account acc = account(1L);
        Category cat = category(2L);

        Transaction p1 = transaction(50L, acc, cat); p1.setInstallmentGroupId(50L); p1.setInstallmentIndex(1); p1.setApplied(true);
        Transaction p2 = transaction(51L, acc, cat); p2.setInstallmentGroupId(50L); p2.setInstallmentIndex(2); p2.setApplied(true);
        Transaction p3 = transaction(52L, acc, cat); p3.setInstallmentGroupId(50L); p3.setInstallmentIndex(3); p3.setApplied(true);

        TransactionRequest req = new TransactionRequest(1L, 2L, null, 200.0,
                LocalDate.of(2025, 1, 15), TransactionType.DEBIT, 4, null, null);

        when(transactionRepository.findById(50L)).thenReturn(Optional.of(p1));
        when(transactionRepository.findByInstallmentGroupIdOrderByInstallmentIndexAsc(50L)).thenReturn(List.of(p1, p2, p3));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            if (t.getId() == null) t.setId(900L);
            return t;
        });

        TransactionResponse resp = transactionService.update(50L, 1L, req);

        assertThat(resp.id()).isEqualTo(50L);
        verify(transactionRepository).deleteById(51L);
        verify(transactionRepository).deleteById(52L);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, atLeast(4)).save(captor.capture());
        Map<Integer, Double> byIndex = new HashMap<>();
        for (Transaction t : captor.getAllValues()) byIndex.put(t.getInstallmentIndex(), t.getValue());
        assertThat(byIndex.keySet()).containsExactlyInAnyOrder(1, 2, 3, 4);
        assertThat(byIndex.values().stream().mapToDouble(Double::doubleValue).sum()).isCloseTo(200.0, within(0.001));

        verify(historyService).recordChanges(eq(HistoryService.ENTITY_TRANSACTION), eq(50L), eq(1L), anyMap());
    }
}
