package com.financecontrol.service;

import com.financecontrol.dto.request.PayInvoiceRequest;
import com.financecontrol.dto.response.InvoiceResponse;
import com.financecontrol.dto.response.TransactionResponse;
import com.financecontrol.entity.Account;
import com.financecontrol.entity.CreditCardInvoicePayment;
import com.financecontrol.entity.Transaction;
import com.financecontrol.enums.AccountType;
import com.financecontrol.enums.TransactionType;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.repository.AccountRepository;
import com.financecontrol.repository.CreditCardInvoicePaymentRepository;
import com.financecontrol.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class CreditCardInvoiceServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock CreditCardInvoicePaymentRepository paymentRepository;
    @Mock TransferService transferService;

    @InjectMocks CreditCardInvoiceService service;

    private static TransactionResponse txResp(Long id) {
        return new TransactionResponse(id, null, null, null, 200.0, LocalDate.now(),
                TransactionType.DEBIT, null, null, 0L, null, null, null, null, LocalDateTime.now());
    }

    private static Account card(Long id, int closingDay, int dueDay) {
        Account a = new Account();
        a.setId(id);
        a.setUserId(1L);
        a.setType(AccountType.CREDIT_CARD);
        a.setClosingDay(closingDay);
        a.setDueDay(dueDay);
        a.setBalance(0.0);
        return a;
    }

    private static Transaction tx(Long id, Account acc, double value, LocalDate date, TransactionType type) {
        Transaction t = new Transaction();
        t.setId(id);
        t.setAccount(acc);
        t.setValue(value);
        t.setDate(date);
        t.setType(type);
        return t;
    }

    @Test
    void listInvoices_agrupaLancamentosPorCicloDeFechamento() {
        Account c = card(1L, 10, 20);
        Transaction t1 = tx(10L, c, 100.0, LocalDate.of(2026, 6, 5), TransactionType.DEBIT);
        Transaction t2 = tx(11L, c, 50.0, LocalDate.of(2026, 6, 15), TransactionType.DEBIT);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(c));
        when(transactionRepository.findByAccount_IdOrderByDateAsc(1L)).thenReturn(List.of(t1, t2));
        when(paymentRepository.findByAccount_Id(1L)).thenReturn(List.of());

        List<InvoiceResponse> invoices = service.listInvoices(1L);

        assertThat(invoices).hasSize(2);
        assertThat(invoices.get(0).referenceMonth()).isEqualTo("2026-07");
        assertThat(invoices.get(0).total()).isEqualTo(50.0);
        assertThat(invoices.get(0).closingDate()).isEqualTo(LocalDate.of(2026, 7, 10));
        assertThat(invoices.get(0).dueDate()).isEqualTo(LocalDate.of(2026, 7, 20));
        assertThat(invoices.get(1).referenceMonth()).isEqualTo("2026-06");
        assertThat(invoices.get(1).total()).isEqualTo(100.0);
    }

    @Test
    void listInvoices_naoSendoCartao_lancaBusinessException() {
        Account checking = new Account();
        checking.setId(1L);
        checking.setType(AccountType.CHECKING);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(checking));

        assertThatThrownBy(() -> service.listInvoices(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("notCreditCard");
    }

    @Test
    void pay_sucesso_criaTransferenciaERegistraPagamento() {
        Account c = card(1L, 10, 20);
        Transaction t1 = tx(10L, c, 200.0, LocalDate.of(2026, 5, 5), TransactionType.DEBIT);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(c));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(card(2L, 10, 20)));
        when(transactionRepository.findByAccount_IdOrderByDateAsc(1L)).thenReturn(List.of(t1));
        when(paymentRepository.findByAccount_Id(1L)).thenReturn(List.of());
        when(paymentRepository.findByAccount_IdAndReferenceMonth(1L, "2026-05")).thenReturn(Optional.empty());
        when(transferService.create(eq(1L), any())).thenReturn(txResp(600L));

        InvoiceResponse resp = service.pay(1L, 1L, "2026-05", new PayInvoiceRequest(2L, 9L, null));

        assertThat(resp.status()).isEqualTo("PAID");
        assertThat(resp.paymentTransactionId()).isEqualTo(600L);
        verify(transferService).create(eq(1L), any());
        verify(paymentRepository).save(any(CreditCardInvoicePayment.class));
    }

    @Test
    void pay_faturaJaPaga_lancaBusinessException() {
        Account c = card(1L, 10, 20);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(c));
        when(paymentRepository.findByAccount_IdAndReferenceMonth(1L, "2026-05"))
                .thenReturn(Optional.of(new CreditCardInvoicePayment()));

        assertThatThrownBy(() -> service.pay(1L, 1L, "2026-05", new PayInvoiceRequest(2L, 9L, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("alreadyPaid");
    }
}
