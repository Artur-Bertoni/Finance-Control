package com.financecontrol.service;

import com.financecontrol.dto.request.AccountRequest;
import com.financecontrol.dto.response.AccountResponse;
import com.financecontrol.entity.Account;
import com.financecontrol.entity.FinancialInstitution;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.AccountRepository;
import com.financecontrol.repository.FinancialInstitutionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings({"null", "unchecked"})
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock FinancialInstitutionRepository financialInstitutionRepository;
    @Mock HistoryService historyService;

    @InjectMocks AccountService accountService;

    @Test
    void findAllByUser_retornaContasDoUsuario() {
        Long userId = 1L;
        FinancialInstitution fi = fiWith(1L, "Nubank");
        Account account = accountWith(1L, userId, fi, "Carteira");

        when(accountRepository.findByUserIdOrderByNameAsc(userId)).thenReturn(List.of(account));

        List<AccountResponse> result = accountService.findAllByUser(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Carteira");
    }

    @Test
    void create_sucesso_registraHistorico() {
        Long userId = 1L;
        AccountRequest req = new AccountRequest(1L, "Nubank", null, null, 500.0, null);
        FinancialInstitution fi = fiWith(1L, "Nubank");

        when(accountRepository.existsByUserIdAndNameIgnoreCase(userId, "Nubank")).thenReturn(false);
        when(financialInstitutionRepository.findById(1L)).thenReturn(Optional.of(fi));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            a.setId(10L);
            return a;
        });

        AccountResponse resp = accountService.create(userId, req, false);

        assertThat(resp.name()).isEqualTo("Nubank");
        assertThat(resp.balance()).isEqualTo(500.0);
        assertThat(resp.type()).isEqualTo(com.financecontrol.enums.AccountType.CHECKING);
        verify(historyService).recordCreation(HistoryService.ENTITY_ACCOUNT, 10L, userId);
    }

    @Test
    void create_comTypeCreditCard_persisteTipo() {
        Long userId = 1L;
        AccountRequest req = new AccountRequest(1L, "Cartao", null, null, 0.0, null,
                com.financecontrol.enums.AccountType.CREDIT_CARD, null, null);
        FinancialInstitution fi = fiWith(1L, "Banco");

        when(accountRepository.existsByUserIdAndNameIgnoreCase(userId, "Cartao")).thenReturn(false);
        when(financialInstitutionRepository.findById(1L)).thenReturn(Optional.of(fi));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            a.setId(11L);
            return a;
        });

        AccountResponse resp = accountService.create(userId, req, false);

        assertThat(resp.type()).isEqualTo(com.financecontrol.enums.AccountType.CREDIT_CARD);
    }

    @Test
    void create_nomeRepetido_lancaBusinessException() {
        Long userId = 1L;
        AccountRequest req = new AccountRequest(1L, "Nubank", null, null, 0.0, null);

        when(accountRepository.existsByUserIdAndNameIgnoreCase(userId, "Nubank")).thenReturn(true);

        assertThatThrownBy(() -> accountService.create(userId, req, false))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void create_nomeRepetidoComForce_salvaSemErro() {
        Long userId = 1L;
        AccountRequest req = new AccountRequest(1L, "Nubank", null, null, 0.0, null);
        FinancialInstitution fi = fiWith(1L, "Nubank");

        when(financialInstitutionRepository.findById(1L)).thenReturn(Optional.of(fi));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            a.setId(2L);
            return a;
        });

        assertThatCode(() -> accountService.create(userId, req, true)).doesNotThrowAnyException();
        verify(accountRepository, never()).existsByUserIdAndNameIgnoreCase(any(), any());
    }

    @Test
    void create_instituicaoNaoEncontrada_lancaResourceNotFoundException() {
        Long userId = 1L;
        AccountRequest req = new AccountRequest(99L, "X", null, null, 0.0, null);

        when(accountRepository.existsByUserIdAndNameIgnoreCase(userId, "X")).thenReturn(false);
        when(financialInstitutionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.create(userId, req, false))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_sucesso_chamaRepository() {
        Account account = accountWith(5L, 1L, fiWith(1L, "B"), "Test");
        when(accountRepository.findById(5L)).thenReturn(Optional.of(account));

        accountService.delete(5L);

        verify(accountRepository).deleteById(5L);
    }

    @Test
    void delete_contaNaoEncontrada_lancaResourceNotFoundException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_sucesso_salvaERegistraDiff() {
        Long userId = 1L;
        FinancialInstitution fi = fiWith(1L, "Nubank");
        Account account = accountWith(5L, userId, fi, "Antiga");
        AccountRequest req = new AccountRequest(1L, "Nova", null, null, 999.0, null);

        when(accountRepository.findById(5L)).thenReturn(Optional.of(account));
        when(financialInstitutionRepository.findById(1L)).thenReturn(Optional.of(fi));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse resp = accountService.update(5L, userId, req);

        assertThat(resp.name()).isEqualTo("Nova");
        assertThat(resp.balance()).isEqualTo(999.0);
        verify(accountRepository).save(account);
        verify(historyService).recordChanges(eq(HistoryService.ENTITY_ACCOUNT), eq(5L), eq(userId), anyMap());
    }

    @Test
    void update_contaNaoEncontrada_lancaResourceNotFoundException() {
        AccountRequest req = new AccountRequest(1L, "Nova", null, null, 0.0, null);
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.update(99L, 1L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_instituicaoNaoEncontrada_lancaResourceNotFoundException() {
        FinancialInstitution fi = fiWith(1L, "Nubank");
        Account account = accountWith(5L, 1L, fi, "Antiga");
        AccountRequest req = new AccountRequest(99L, "Nova", null, null, 0.0, null);

        when(accountRepository.findById(5L)).thenReturn(Optional.of(account));
        when(financialInstitutionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.update(5L, 1L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void totalValue_retornaSumBalance() {
        when(accountRepository.sumBalance(1L, 5L)).thenReturn(1234.0);

        assertThat(accountService.totalValue(1L, 5L)).isEqualTo(1234.0);
    }

    @Test
    void patchBalance_chamaRepositoryERegistraHistorico() {
        Account account = accountWith(5L, 1L, fiWith(1L, "Nubank"), "Conta");
        account.setBalance(100.0);
        when(accountRepository.findById(5L)).thenReturn(Optional.of(account));

        accountService.patchBalance(5L, 50.0);

        verify(accountRepository).patchBalance(5L, 50.0);
        verify(historyService).recordChanges(eq(HistoryService.ENTITY_ACCOUNT), eq(5L), eq(1L), anyMap());
    }

    @Test
    void findById_naoEncontrada_lancaResourceNotFoundException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── buildDiff per-field branch coverage ──────────────────────────────────

    private Account fullAccount() {
        FinancialInstitution fi = fiWith(1L, "Nubank");
        Account a = new Account();
        a.setId(5L);
        a.setUserId(1L);
        a.setFinancialInstitution(fi);
        a.setName("Conta");
        a.setContact("contato@x.com");
        a.setDescription("desc original");
        a.setBalance(100.0);
        a.setIconKey("ph-wallet");
        return a;
    }

    private AccountResponse updateWith(Account existing, AccountRequest req) {
        when(accountRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(financialInstitutionRepository.findById(req.financialInstitutionId()))
                .thenReturn(Optional.of(existing.getFinancialInstitution()));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        return accountService.update(5L, 1L, req);
    }

    @Test
    void update_mudaApenasNome_registraDiffNome() {
        Account existing = fullAccount();
        AccountRequest req = new AccountRequest(1L, "Conta Nova", "contato@x.com", "desc original", 100.0, "ph-wallet");

        updateWith(existing, req);

        ArgumentCaptor<Map<String, String[]>> diff = ArgumentCaptor.forClass(Map.class);
        verify(historyService).recordChanges(eq(HistoryService.ENTITY_ACCOUNT), eq(5L), eq(1L), diff.capture());
        assertThat(diff.getValue()).containsOnlyKeys("name");
    }

    @Test
    void update_mudaApenasSaldo_registraDiffBalance() {
        Account existing = fullAccount();
        AccountRequest req = new AccountRequest(1L, "Conta", "contato@x.com", "desc original", 555.0, "ph-wallet");

        updateWith(existing, req);

        ArgumentCaptor<Map<String, String[]>> diff = ArgumentCaptor.forClass(Map.class);
        verify(historyService).recordChanges(eq(HistoryService.ENTITY_ACCOUNT), eq(5L), eq(1L), diff.capture());
        assertThat(diff.getValue()).containsOnlyKeys("balance");
    }

    @Test
    void update_mudaApenasContato_registraDiffContact() {
        Account existing = fullAccount();
        AccountRequest req = new AccountRequest(1L, "Conta", "novo@x.com", "desc original", 100.0, "ph-wallet");

        updateWith(existing, req);

        ArgumentCaptor<Map<String, String[]>> diff = ArgumentCaptor.forClass(Map.class);
        verify(historyService).recordChanges(eq(HistoryService.ENTITY_ACCOUNT), eq(5L), eq(1L), diff.capture());
        assertThat(diff.getValue()).containsOnlyKeys("contact");
    }

    @Test
    void update_mudaApenasDescricao_registraDiffDescription() {
        Account existing = fullAccount();
        AccountRequest req = new AccountRequest(1L, "Conta", "contato@x.com", "desc nova", 100.0, "ph-wallet");

        updateWith(existing, req);

        ArgumentCaptor<Map<String, String[]>> diff = ArgumentCaptor.forClass(Map.class);
        verify(historyService).recordChanges(eq(HistoryService.ENTITY_ACCOUNT), eq(5L), eq(1L), diff.capture());
        assertThat(diff.getValue()).containsOnlyKeys("description");
    }

    @Test
    void update_mudaApenasIconKey_registraDiffIconKey() {
        Account existing = fullAccount();
        AccountRequest req = new AccountRequest(1L, "Conta", "contato@x.com", "desc original", 100.0, "ph-bank");

        updateWith(existing, req);

        ArgumentCaptor<Map<String, String[]>> diff = ArgumentCaptor.forClass(Map.class);
        verify(historyService).recordChanges(eq(HistoryService.ENTITY_ACCOUNT), eq(5L), eq(1L), diff.capture());
        assertThat(diff.getValue()).containsOnlyKeys("iconKey");
    }

    @Test
    void update_mudaApenasInstituicao_registraDiffFinancialInstitution() {
        Account existing = fullAccount();
        FinancialInstitution novaFi = fiWith(2L, "Itau");
        AccountRequest req = new AccountRequest(2L, "Conta", "contato@x.com", "desc original", 100.0, "ph-wallet");

        when(accountRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(financialInstitutionRepository.findById(2L)).thenReturn(Optional.of(novaFi));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        accountService.update(5L, 1L, req);

        ArgumentCaptor<Map<String, String[]>> diff = ArgumentCaptor.forClass(Map.class);
        verify(historyService).recordChanges(eq(HistoryService.ENTITY_ACCOUNT), eq(5L), eq(1L), diff.capture());
        assertThat(diff.getValue()).containsOnlyKeys("financialInstitution");
    }

    private static FinancialInstitution fiWith(Long id, String name) {
        FinancialInstitution fi = new FinancialInstitution();
        fi.setId(id);
        fi.setName(name);
        return fi;
    }

    private static Account accountWith(Long id, Long userId, FinancialInstitution fi, String name) {
        Account a = new Account();
        a.setId(id);
        a.setUserId(userId);
        a.setFinancialInstitution(fi);
        a.setName(name);
        a.setBalance(0.0);
        return a;
    }
}
