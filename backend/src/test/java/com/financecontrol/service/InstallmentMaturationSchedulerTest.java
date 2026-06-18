package com.financecontrol.service;

import com.financecontrol.entity.Account;
import com.financecontrol.entity.Transaction;
import com.financecontrol.enums.TransactionType;
import com.financecontrol.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class InstallmentMaturationSchedulerTest {

    @Mock TransactionRepository transactionRepository;
    @Mock AccountService        accountService;

    private InstallmentMaturationScheduler scheduler() {
        return new InstallmentMaturationScheduler(transactionRepository, accountService, "America/Sao_Paulo");
    }

    private static Transaction parcela(Long id, Long accountId, double value, TransactionType type) {
        Account acc = new Account();
        acc.setId(accountId);
        Transaction t = new Transaction();
        t.setId(id);
        t.setAccount(acc);
        t.setValue(value);
        t.setType(type);
        t.setApplied(false);
        return t;
    }

    @Test
    void matureDueInstallments_aplicaSaldoEmarcaComoAplicada() {
        Transaction t = parcela(1L, 9L, 50.0, TransactionType.DEBIT);
        when(transactionRepository.findUnappliedDue(any(LocalDate.class))).thenReturn(List.of(t));

        scheduler().matureDueInstallments();

        verify(accountService).patchBalance(9L, -50.0);
        verify(transactionRepository).save(t);
        org.assertj.core.api.Assertions.assertThat(t.getApplied()).isTrue();
    }

    @Test
    void matureDueInstallments_semParcelasVencidas_naoFazNada() {
        when(transactionRepository.findUnappliedDue(any(LocalDate.class))).thenReturn(List.of());

        scheduler().matureDueInstallments();

        verifyNoInteractions(accountService);
        verify(transactionRepository, never()).save(any());
    }
}
