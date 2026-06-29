package com.financecontrol.service;

import com.financecontrol.dto.request.TransactionRequest;
import com.financecontrol.dto.response.TransactionResponse;
import com.financecontrol.entity.*;
import com.financecontrol.enums.TransactionType;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.financecontrol.service.HistoryService.*;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final TransactionType TYPE_CREDIT = TransactionType.CREDIT;
    private static final TransactionType TYPE_DEBIT  = TransactionType.DEBIT;
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final String FIELD_INSTALLMENTS = "installmentsNumber";

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionLocaleRepository transactionLocaleRepository;
    private final HistoryService historyService;
    @Lazy private final AccountService accountService;

    @Transactional(readOnly = true)
    @Cacheable(value = "transactions", key = "{ #userId, #startDate, #endDate, #categoryId, #accountId }")
    public List<TransactionResponse> findAllByUser(Long userId,
                                                   LocalDate startDate,
                                                   LocalDate endDate,
                                                   Long categoryId,
                                                   Long accountId) {
        return transactionRepository.findAllFiltered(userId, startDate, endDate, categoryId, accountId).stream().map(TransactionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public TransactionResponse findById(@NonNull Long id) {
        Transaction t = getOrThrow(id);
        if (isInstallmentGroup(t.getInstallmentGroupId())) {
            double total = transactionRepository.findByInstallmentGroupIdOrderByInstallmentIndexAsc(t.getInstallmentGroupId())
                    .stream().mapToDouble(Transaction::getValue).sum();
            return TransactionResponse.from(t, Math.round(total * 100) / 100.0);
        }
        return TransactionResponse.from(t);
    }

    @Transactional
    @SuppressWarnings("null")
    @CacheEvict(value = "transactions", allEntries = true)
    public TransactionResponse create(Long userId,
                                      TransactionRequest req,
                                      boolean force) {
        int installments = req.installmentsNumber() != null ? req.installmentsNumber() : 0;
        if (installments >= 2) {
            return createInstallments(userId, req, installments);
        }

        if (!force && transactionRepository.existsDuplicate(
                userId, req.accountId(), req.categoryId(), req.transactionLocaleId(),
                req.value(), req.date(), req.type(), req.installmentsNumber(), req.obs()))
            throw new BusinessException("error.duplicate.transaction");

        applyBalanceDelta(req.accountId(), req.type(), req.value());

        TransactionResponse result = TransactionResponse.from(transactionRepository.save(buildEntity(userId, req)));
        historyService.recordCreation(ENTITY_TRANSACTION, result.id(), userId);

        return result;
    }

    @SuppressWarnings("null")
    private TransactionResponse createInstallments(Long userId,
                                                   TransactionRequest req,
                                                   int n) {
        TransactionDeps deps = loadDeps(req);
        LocalDate today = LocalDate.now(ZONE);

        long totalCents = Math.round((req.value() != null ? req.value() : 0.0) * 100);
        long base       = totalCents / n;
        long remainder  = totalCents - base * n;

        Long groupId      = null;
        Transaction first = null;

        for (int k = 0; k < n; k++) {
            long cents          = base + (k < remainder ? 1 : 0);
            double parcelaValue = cents / 100.0;
            LocalDate date      = req.date() != null ? req.date().plusMonths(k) : null;
            boolean applied     = date == null || !date.isAfter(today);

            Transaction t = new Transaction(null, userId, deps.account(), deps.category(), deps.locale(),
                    parcelaValue, date, req.type(), n, req.obs(),
                    0L, LocalDateTime.now(ZONE), groupId, k + 1, applied);
            t = transactionRepository.save(t);

            if (k == 0) {
                groupId = t.getId();
                t.setInstallmentGroupId(groupId);
                first = transactionRepository.save(t);
            }

            if (applied) {
                applyBalanceDelta(req.accountId(), req.type(), parcelaValue);
            }
        }

        TransactionResponse result = TransactionResponse.from(first);
        historyService.recordCreation(ENTITY_TRANSACTION, result.id(), userId);

        return result;
    }

    @SuppressWarnings({"null", "java:S3776"})
    private TransactionResponse updateInstallmentGroup(Transaction anyMember,
                                                       Long userId,
                                                       TransactionRequest req) {
        Long groupId = anyMember.getInstallmentGroupId();
        List<Transaction> group = transactionRepository.findByInstallmentGroupIdOrderByInstallmentIndexAsc(groupId);

        Transaction parent = group.stream()
                .filter(t -> groupId.equals(t.getId()))
                .findFirst().orElse(group.get(0));

        TransactionDeps deps = loadDeps(req);
        LocalDate today = LocalDate.now(ZONE);

        for (Transaction p : group) {
            Long accId = p.getAccount() != null ? p.getAccount().getId() : null;
            if (accId != null && isApplied(p)) {
                accountService.patchBalance(accId, revertDelta(p));
            }
            if (!p.getId().equals(parent.getId())) {
                transactionRepository.deleteById(p.getId());
            }
        }

        int n = req.installmentsNumber() != null ? req.installmentsNumber() : 0;

        if (n < 2) {
            boolean applied = req.date() == null || !req.date().isAfter(today);
            applyUpdateWithDeps(parent, userId, req, deps);
            parent.setInstallmentGroupId(null);
            parent.setInstallmentIndex(null);
            parent.setApplied(applied);
            if (applied) applyBalanceDelta(req.accountId(), req.type(), req.value());

            Transaction saved = transactionRepository.save(parent);
            historyService.recordChanges(ENTITY_TRANSACTION, parent.getId(), userId,
                    Map.of(FIELD_INSTALLMENTS, diff(String.valueOf(group.size()), "0")));
            return TransactionResponse.from(saved);
        }

        long totalCents = Math.round((req.value() != null ? req.value() : 0.0) * 100);
        long base       = totalCents / n;
        long remainder  = totalCents - base * n;

        Transaction savedParent = parent;
        for (int k = 0; k < n; k++) {
            long cents          = base + (k < remainder ? 1 : 0);
            double parcelaValue = cents / 100.0;
            LocalDate date      = req.date() != null ? req.date().plusMonths(k) : null;
            boolean applied     = date == null || !date.isAfter(today);

            Transaction t = (k == 0) ? parent
                    : new Transaction(null, userId, deps.account(), deps.category(), deps.locale(),
                            parcelaValue, date, req.type(), n, req.obs(),
                            0L, LocalDateTime.now(ZONE), parent.getId(), k + 1, applied);
            t.setUserId(userId);
            t.setAccount(deps.account());
            t.setCategory(deps.category());
            t.setTransactionLocale(deps.locale());
            t.setType(req.type());
            t.setObs(req.obs());
            t.setTransferPartnerId(0L);
            t.setInstallmentGroupId(parent.getId());
            t.setValue(parcelaValue);
            t.setDate(date);
            t.setInstallmentsNumber(n);
            t.setInstallmentIndex(k + 1);
            t.setApplied(applied);

            Transaction saved = transactionRepository.save(t);
            if (k == 0) savedParent = saved;
            if (applied) applyBalanceDelta(req.accountId(), req.type(), parcelaValue);
        }

        historyService.recordChanges(ENTITY_TRANSACTION, parent.getId(), userId,
                Map.of(FIELD_INSTALLMENTS, diff(String.valueOf(group.size()), String.valueOf(n))));
        return TransactionResponse.from(savedParent);
    }

    @Transactional
    @CacheEvict(value = "transactions", allEntries = true)
    public TransactionResponse update(@NonNull Long id,
                                      Long userId,
                                      TransactionRequest req) {
        return updateOne(id, userId, req);
    }

    @Transactional
    @CacheEvict(value = "transactions", allEntries = true)
    public void delete(@NonNull Long id) {
        deleteOne(id);
    }

    @Transactional
    @CacheEvict(value = "transactions", allEntries = true)
    public TransactionResponse patchTransferPartner(@NonNull Long id,
                                                    @Nullable Long partnerId) {
        Transaction t = getOrThrow(id);
        t.setTransferPartnerId(partnerId);

        return TransactionResponse.from(transactionRepository.save(t));
    }

    @NonNull
    @SuppressWarnings("null")
    Transaction getOrThrow(@NonNull Long id) {
        return transactionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("error.notFound.transaction"));
    }

    @SuppressWarnings({"null", "java:S3776"})
    private TransactionResponse updateOne(@NonNull Long id,
                                          Long userId,
                                          TransactionRequest req) {
        Transaction existing = getOrThrow(id);

        if (isInstallmentGroup(existing.getInstallmentGroupId())) {
            return updateInstallmentGroup(existing, userId, req);
        }

        TransactionType oldType = existing.getType();
        double revert = TYPE_CREDIT == oldType ? -existing.getValue() : existing.getValue();
        Long accountId = existing.getAccount().getId();

        boolean wasApplied = isApplied(existing);
        boolean nowApplied = !isInstallmentGroup(existing.getInstallmentGroupId())
                || req.date() == null || !req.date().isAfter(LocalDate.now(ZONE));

        if (accountId != null && wasApplied) {
            accountService.patchBalance(accountId, revert);
        }
        if (nowApplied) {
            applyBalanceDelta(req.accountId(), req.type(), req.value());
        }
        existing.setApplied(nowApplied);

        Long partnerId = existing.getTransferPartnerId();
        if (isTransferPartner(partnerId)) {
            Transaction partner = getOrThrow(partnerId);
            if (!partner.getValue().equals(req.value())) {
                TransactionType partnerType = TYPE_DEBIT == req.type() ? TYPE_CREDIT : TYPE_DEBIT;
                Long partnerAccountId = partner.getAccount().getId();
                TransactionRequest partnerReq = new TransactionRequest(
                        partnerAccountId, req.categoryId(), req.transactionLocaleId(),
                        req.value(), req.date(), partnerType,
                        req.installmentsNumber(), req.obs(), id);
                Long partnerId2 = partner.getId();
                if (partnerId2 != null) {
                    updateOne(partnerId2, userId, partnerReq);
                }
            }
        }

        TransactionDeps newDeps = loadDeps(req);
        Map<String, String[]> diff = buildDiff(existing, req, newDeps);

        applyUpdateWithDeps(existing, userId, req, newDeps);

        TransactionResponse result = TransactionResponse.from(transactionRepository.save(existing));
        historyService.recordChanges(ENTITY_TRANSACTION, id, userId, diff);

        return result;
    }

    @SuppressWarnings("null")
    private void deleteOne(@NonNull Long id) {
        Transaction t = getOrThrow(id);

        Long groupId = t.getInstallmentGroupId();
        if (isInstallmentGroup(groupId)) {
            for (Transaction p : transactionRepository.findByInstallmentGroupIdOrderByInstallmentIndexAsc(groupId)) {
                Long pAccountId = p.getAccount().getId();
                if (pAccountId != null && isApplied(p)) {
                    accountService.patchBalance(pAccountId, revertDelta(p));
                }
                transactionRepository.deleteById(p.getId());
            }
            return;
        }

        Long accountId = t.getAccount().getId();
        if (accountId != null && isApplied(t)) {
            accountService.patchBalance(accountId, revertDelta(t));
        }

        Long partnerId = t.getTransferPartnerId();
        if (isTransferPartner(partnerId) && transactionRepository.existsById(partnerId)) {
            deleteOne(partnerId);
        }

        transactionRepository.deleteById(id);
    }

    private void applyBalanceDelta(@NonNull Long accountId,
                                   TransactionType type,
                                   Double value) {
        double delta = TYPE_CREDIT == type ? value : -value;
        accountService.patchBalance(accountId, delta);
    }

    private boolean isTransferPartner(Long partnerId) {
        return partnerId != null && !partnerId.equals(0L);
    }

    private boolean isInstallmentGroup(Long groupId) {
        return groupId != null && !groupId.equals(0L);
    }

    private boolean isApplied(Transaction t) {
        return !Boolean.FALSE.equals(t.getApplied());
    }

    private double revertDelta(Transaction t) {
        return TYPE_CREDIT == t.getType() ? -t.getValue() : t.getValue();
    }

    private record TransactionDeps(Account account, Category category, TransactionLocale locale) {}

    @SuppressWarnings("null")
    private TransactionDeps loadDeps(TransactionRequest req) {
        Account account = accountRepository.findById(req.accountId()).orElseThrow(() -> new ResourceNotFoundException("error.notFound.account"));
        Category category = categoryRepository.findById(req.categoryId()).orElseThrow(() -> new ResourceNotFoundException("error.notFound.category"));

        Long localeId = req.transactionLocaleId();
        
        TransactionLocale locale = localeId != null
                ? transactionLocaleRepository.findById(localeId).orElse(null)
                : null;
        
                return new TransactionDeps(account, category, locale);
    }

    private Transaction buildEntity(Long userId,
                                    TransactionRequest req) {
        TransactionDeps deps = loadDeps(req);
        Integer installmentsNumber = req.installmentsNumber() != null ? req.installmentsNumber() : 0;
        Long transferPartnerId = req.transferPartnerId() != null ? req.transferPartnerId() : 0L;

        return new Transaction(null, userId, deps.account(), deps.category(), deps.locale(),
                req.value(), req.date(), req.type(), installmentsNumber, req.obs(),
                transferPartnerId, LocalDateTime.now(ZONE), null, null, true);
    }

    private void applyUpdateWithDeps(Transaction transaction,
                                     Long userId,
                                     TransactionRequest req,
                                     TransactionDeps deps) {
        transaction.setUserId(userId);
        transaction.setAccount(deps.account());
        transaction.setCategory(deps.category());
        transaction.setTransactionLocale(deps.locale());
        transaction.setValue(req.value());
        transaction.setDate(req.date());
        transaction.setType(req.type());
        transaction.setInstallmentsNumber(req.installmentsNumber() != null ? req.installmentsNumber() : 0);
        transaction.setObs(req.obs());
        transaction.setTransferPartnerId(req.transferPartnerId() != null ? req.transferPartnerId() : 0L);
    }

    @SuppressWarnings("java:S3776")
    private Map<String, String[]> buildDiff(Transaction t,
                                            TransactionRequest req,
                                            TransactionDeps newDeps) {
        Map<String, String[]> diff = new LinkedHashMap<>();

        if (differs(t.getAccount().getId(), req.accountId()))
            diff.put("account", diff(t.getAccount().getName(), newDeps.account().getName()));
        if (differs(t.getCategory().getId(), req.categoryId()))
            diff.put("category", diff(t.getCategory().getName(), newDeps.category().getName()));
        if (differs(t.getValue(), req.value()))
            diff.put("value", diff(t.getValue(), req.value()));
        if (differs(t.getDate(), req.date()))
            diff.put("date", diff(t.getDate() != null ? t.getDate().toString() : null, req.date() != null ? req.date().toString() : null));
        if (differs(t.getType(), req.type()))
            diff.put("type", diff(t.getType() != null ? t.getType().getValue() : null, req.type() != null ? req.type().getValue() : null));

        int oldInstall = t.getInstallmentsNumber() != null ? t.getInstallmentsNumber() : 0;
        int newInstall = req.installmentsNumber() != null ? req.installmentsNumber() : 0;

        if (oldInstall != newInstall)
            diff.put(FIELD_INSTALLMENTS, diff(String.valueOf(oldInstall), String.valueOf(newInstall)));
        if (differs(t.getObs(), req.obs()))
            diff.put("obs", diff(t.getObs(), req.obs()));

        String oldLocale = t.getTransactionLocale() != null ? t.getTransactionLocale().getName() : null;
        String newLocale = newDeps.locale() != null ? newDeps.locale().getName() : null;
        Long oldLocaleId = t.getTransactionLocale() != null ? t.getTransactionLocale().getId() : null;

        if (differs(oldLocaleId, req.transactionLocaleId()))
            diff.put("transactionLocale", diff(oldLocale, newLocale));

        return diff;
    }
}
