package com.financecontrol.service;

import com.financecontrol.dto.request.TransferRequest;
import com.financecontrol.dto.response.TransactionResponse;
import com.financecontrol.entity.Account;
import com.financecontrol.entity.FinancialInstitution;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock TransactionService transactionService;
    @Mock AccountRepository accountRepository;

    @InjectMocks TransferService transferService;

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void create_sucesso_criaDuasTransacoesEPatcheaParcerias() {
        TransferRequest req = new TransferRequest(1L, 2L, null, null, 100.0, LocalDate.now(), null);
        Account origin = accountWith(1L, 500.0);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(origin));
        TransactionResponse originTx  = txResponse(10L);
        TransactionResponse destTx    = txResponse(11L);
        when(transactionService.create(eq(1L), any(), eq(true)))
                .thenReturn(originTx)
                .thenReturn(destTx);

        transferService.create(1L, req);

        verify(transactionService, times(2)).create(eq(1L), any(), eq(true));
        verify(transactionService).patchTransferPartner(10L, 11L);
        verify(transactionService).patchTransferPartner(11L, 10L);
    }

    @Test
    void create_mesmaContaOrigemEDestino_lancaBusinessException() {
        TransferRequest req = new TransferRequest(1L, 1L, null, null, 100.0, LocalDate.now(), null);

        assertThatThrownBy(() -> transferService.create(1L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("sameAccount");
    }

    @Test
    void create_contaOrigemNaoEncontrada_lancaResourceNotFoundException() {
        TransferRequest req = new TransferRequest(99L, 2L, null, null, 100.0, LocalDate.now(), null);
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.create(1L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_saldoInsuficiente_lancaBusinessException() {
        TransferRequest req = new TransferRequest(1L, 2L, null, null, 1000.0, LocalDate.now(), null);
        Account origin = accountWith(1L, 50.0);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(origin));

        assertThatThrownBy(() -> transferService.create(1L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("insufficientBalance");
    }

    @Test
    void create_saldoNulo_tratadoComoZero_lancaBusinessException() {
        TransferRequest req = new TransferRequest(1L, 2L, null, null, 10.0, LocalDate.now(), null);
        Account origin = accountWith(1L, null);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(origin));

        assertThatThrownBy(() -> transferService.create(1L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("insufficientBalance");
    }

    @Test
    void create_idsDeTransacaoNulos_naoPatcheaParcerias() {
        TransferRequest req = new TransferRequest(1L, 2L, null, null, 50.0, LocalDate.now(), null);
        Account origin = accountWith(1L, 200.0);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(origin));
        when(transactionService.create(eq(1L), any(), eq(true)))
                .thenReturn(txResponseNullId())
                .thenReturn(txResponseNullId());

        transferService.create(1L, req);

        verify(transactionService, never()).patchTransferPartner(any(), any());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static Account accountWith(Long id, Double balance) {
        Account a = new Account();
        a.setId(id);
        a.setUserId(1L);
        a.setBalance(balance);
        FinancialInstitution fi = new FinancialInstitution();
        fi.setId(1L);
        fi.setName("Banco");
        a.setFinancialInstitution(fi);
        a.setName("Conta");
        return a;
    }

    private static TransactionResponse txResponse(Long id) {
        return new TransactionResponse(id, null, null, null, 100.0,
                LocalDate.now(), null, null, null, null, null, null, null, null, null);
    }

    private static TransactionResponse txResponseNullId() {
        return new TransactionResponse(null, null, null, null, 100.0,
                LocalDate.now(), null, null, null, null, null, null, null, null, null);
    }
}
