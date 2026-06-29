package com.financecontrol.service;

import com.financecontrol.dto.request.PayInvoiceRequest;
import com.financecontrol.dto.request.TransferRequest;
import com.financecontrol.dto.response.InvoiceResponse;
import com.financecontrol.dto.response.TransactionResponse;
import com.financecontrol.entity.Account;
import com.financecontrol.entity.CreditCardInvoicePayment;
import com.financecontrol.entity.Transaction;
import com.financecontrol.enums.AccountType;
import com.financecontrol.enums.TransactionType;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.AccountRepository;
import com.financecontrol.repository.CreditCardInvoicePaymentRepository;
import com.financecontrol.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CreditCardInvoiceService {

    private static final DateTimeFormatter REF = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CreditCardInvoicePaymentRepository paymentRepository;
    private final TransferService transferService;

    @Transactional(readOnly = true)
    public List<InvoiceResponse> listInvoices(@NonNull Long accountId) {
        return computeInvoices(accountId);
    }

    private List<InvoiceResponse> computeInvoices(@NonNull Long accountId) {
        Account card = requireCreditCard(accountId);
        int closingDay = card.getClosingDay();
        int dueDay     = card.getDueDay();

        Map<YearMonth, Cycle> cycles = new LinkedHashMap<>();
        for (Transaction t : transactionRepository.findByAccount_IdOrderByDateAsc(accountId)) {
            if (t.getDate() == null) continue;
            LocalDate closing = closingDateFor(t.getDate(), closingDay);
            Cycle cycle = cycles.computeIfAbsent(YearMonth.from(closing), ym -> new Cycle(closing));
            cycle.add(t);
        }

        Map<String, CreditCardInvoicePayment> payments = new LinkedHashMap<>();
        for (CreditCardInvoicePayment p : paymentRepository.findByAccountId(accountId)) {
            payments.put(p.getReferenceMonth(), p);
        }

        LocalDate today = LocalDate.now(ZONE);
        List<InvoiceResponse> result = new ArrayList<>();
        for (Map.Entry<YearMonth, Cycle> e : cycles.entrySet()) {
            String ref = e.getKey().format(REF);
            Cycle c = e.getValue();
            LocalDate dueDate = dueDateFor(c.closing, closingDay, dueDay);
            CreditCardInvoicePayment payment = payments.get(ref);

            result.add(new InvoiceResponse(ref, c.closing, dueDate, round(c.total), c.count,
                    statusOf(payment, c.closing, today), payment != null ? payment.getPaidAt() : null, null));
        }

        result.sort(Comparator.comparing(InvoiceResponse::closingDate).reversed());
        return result;
    }

    private String statusOf(CreditCardInvoicePayment payment, LocalDate closing, LocalDate today) {
        if (payment != null) return "PAID";
        return today.isAfter(closing) ? "CLOSED" : "OPEN";
    }

    @Transactional
    public InvoiceResponse pay(@NonNull Long userId,
                               @NonNull Long accountId,
                               @NonNull String referenceMonth,
                               PayInvoiceRequest req) {
        requireCreditCard(accountId);

        if (paymentRepository.findByAccountIdAndReferenceMonth(accountId, referenceMonth).isPresent())
            throw new BusinessException("error.invoice.alreadyPaid");

        InvoiceResponse invoice = computeInvoices(accountId).stream()
                .filter(i -> i.referenceMonth().equals(referenceMonth))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound.invoice"));

        double total = invoice.total();
        if (total <= 0) throw new BusinessException("error.invoice.nothingToPay");

        LocalDate date = req.date() != null ? req.date() : LocalDate.now(ZONE);
        TransferRequest transfer = new TransferRequest(req.sourceAccountId(), accountId,
                req.categoryId(), null, total, date, "Pagamento fatura " + referenceMonth);
        TransactionResponse cardTx = transferService.create(userId, transfer);

        CreditCardInvoicePayment payment = new CreditCardInvoicePayment(
                null, userId, accountId, referenceMonth, total, req.sourceAccountId(), LocalDateTime.now(ZONE));
        paymentRepository.save(payment);

        return new InvoiceResponse(invoice.referenceMonth(), invoice.closingDate(), invoice.dueDate(),
                invoice.total(), invoice.itemCount(), "PAID", payment.getPaidAt(), cardTx.id());
    }

    private Account requireCreditCard(@NonNull Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound.account"));
        if (account.getType() != AccountType.CREDIT_CARD || account.getClosingDay() == null || account.getDueDay() == null)
            throw new BusinessException("error.invoice.notCreditCard");
        return account;
    }

    private LocalDate closingDateFor(LocalDate date, int closingDay) {
        YearMonth ym = YearMonth.from(date);
        LocalDate candidate = ym.atDay(Math.min(closingDay, ym.lengthOfMonth()));
        if (!date.isAfter(candidate)) return candidate;
        YearMonth next = ym.plusMonths(1);
        return next.atDay(Math.min(closingDay, next.lengthOfMonth()));
    }

    private LocalDate dueDateFor(LocalDate closing, int closingDay, int dueDay) {
        YearMonth dueMonth = dueDay > closingDay ? YearMonth.from(closing) : YearMonth.from(closing).plusMonths(1);
        return dueMonth.atDay(Math.min(dueDay, dueMonth.lengthOfMonth()));
    }

    private double round(double v) {
        return Math.round(v * 100) / 100.0;
    }

    private static final class Cycle {
        private final LocalDate closing;
        private double total;
        private int count;

        private Cycle(LocalDate closing) {
            this.closing = closing;
        }

        private void add(Transaction t) {
            double v = t.getValue() != null ? t.getValue() : 0.0;
            total += TransactionType.CREDIT == t.getType() ? -v : v;
            count++;
        }
    }
}
