package com.financecontrol.service;

import com.financecontrol.dto.request.TransactionRequest;
import com.financecontrol.dto.response.TransactionResponse;
import com.financecontrol.entity.*;
import com.financecontrol.enums.TransactionType;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.financecontrol.service.ChangeHistoryService.*;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final TransactionType TYPE_CREDIT = TransactionType.CREDIT;
    private static final TransactionType TYPE_DEBIT  = TransactionType.DEBIT;

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionLocaleRepository transactionLocaleRepository;
    private final ChangeHistoryService changeHistoryService;
    @Lazy private final AccountService accountService;

    public List<TransactionResponse> findAllByUser(Long userId, LocalDate startDate, LocalDate endDate,
                                                    Long categoryId, Long accountId) {
        return transactionRepository.findAllFiltered(userId, startDate, endDate, categoryId, accountId)
                .stream().map(TransactionResponse::from).toList();
    }

    public TransactionResponse findById(@NonNull Long id) {
        return TransactionResponse.from(getOrThrow(id));
    }

    @Transactional
    @SuppressWarnings("null")
    public TransactionResponse create(Long userId, TransactionRequest req) {
        applyBalanceDelta(req.accountId(), req.type(), req.value());
        TransactionResponse result = TransactionResponse.from(transactionRepository.save(buildEntity(userId, req)));
        changeHistoryService.recordCreation(ENTITY_TRANSACTION, result.id(), userId);
        return result;
    }

    @Transactional
    public TransactionResponse update(@NonNull Long id, Long userId, TransactionRequest req) {
        return updateOne(id, userId, req);
    }

    @Transactional
    public void delete(@NonNull Long id) {
        deleteOne(id);
    }

    @Transactional
    public TransactionResponse patchTransferPartner(@NonNull Long id, @Nullable Long partnerId) {
        Transaction t = getOrThrow(id);
        t.setTransferPartnerId(partnerId);
        return TransactionResponse.from(transactionRepository.save(t));
    }

    @NonNull
    @SuppressWarnings("null")
    Transaction getOrThrow(@NonNull Long id) {
        return transactionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("error.notFound.transaction"));
    }

    @SuppressWarnings("null")
    private TransactionResponse updateOne(@NonNull Long id, Long userId, TransactionRequest req) {
        Transaction existing = getOrThrow(id);

        TransactionType oldType = existing.getType();
        double revert = TYPE_CREDIT == oldType ? -existing.getValue() : existing.getValue();
        Long accountId = existing.getAccount().getId();
        if (accountId != null) {
            accountService.patchBalance(accountId, revert);
        }
        applyBalanceDelta(req.accountId(), req.type(), req.value());

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
        changeHistoryService.recordChanges(ENTITY_TRANSACTION, id, userId, diff);
        return result;
    }

    @SuppressWarnings("null")
    private void deleteOne(@NonNull Long id) {
        Transaction t = getOrThrow(id);
        double revert = TYPE_CREDIT == t.getType() ? -t.getValue() : t.getValue();
        Long accountId = t.getAccount().getId();
        if (accountId != null) {
            accountService.patchBalance(accountId, revert);
        }

        Long partnerId = t.getTransferPartnerId();
        if (isTransferPartner(partnerId) && transactionRepository.existsById(partnerId)) {
            deleteOne(partnerId);
        }

        transactionRepository.deleteById(id);
    }

    private void applyBalanceDelta(@NonNull Long accountId, TransactionType type, Double value) {
        double delta = TYPE_CREDIT == type ? value : -value;
        accountService.patchBalance(accountId, delta);
    }

    private boolean isTransferPartner(Long partnerId) {
        return partnerId != null && !partnerId.equals(0L);
    }

    private record TransactionDeps(Account account, Category category, TransactionLocale locale) {}

    @SuppressWarnings("null")
    private TransactionDeps loadDeps(TransactionRequest req) {
        Account account = accountRepository.findById(req.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound.account"));
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound.category"));
        Long localeId = req.transactionLocaleId();
        TransactionLocale locale = localeId != null
                ? transactionLocaleRepository.findById(localeId).orElse(null) : null;
        return new TransactionDeps(account, category, locale);
    }

    private Transaction buildEntity(Long userId, TransactionRequest req) {
        TransactionDeps deps = loadDeps(req);
        Integer installmentsNumber = req.installmentsNumber() != null ? req.installmentsNumber() : 0;
        Long transferPartnerId = req.transferPartnerId() != null ? req.transferPartnerId() : 0L;
        return new Transaction(null, userId, deps.account(), deps.category(), deps.locale(),
                req.value(), req.date(), req.type(), installmentsNumber, req.obs(),
                transferPartnerId, LocalDateTime.now());
    }

    private void applyUpdateWithDeps(Transaction t, Long userId, TransactionRequest req, TransactionDeps deps) {
        t.setUserId(userId);
        t.setAccount(deps.account());
        t.setCategory(deps.category());
        t.setTransactionLocale(deps.locale());
        t.setValue(req.value());
        t.setDate(req.date());
        t.setType(req.type());
        t.setInstallmentsNumber(req.installmentsNumber() != null ? req.installmentsNumber() : 0);
        t.setObs(req.obs());
        t.setTransferPartnerId(req.transferPartnerId() != null ? req.transferPartnerId() : 0L);
    }

    private Map<String, String[]> buildDiff(Transaction t, TransactionRequest req, TransactionDeps newDeps) {
        Map<String, String[]> diff = new LinkedHashMap<>();
        if (differs(t.getAccount().getId(), req.accountId()))
            diff.put("account", diff(t.getAccount().getName(), newDeps.account().getName()));
        if (differs(t.getCategory().getId(), req.categoryId()))
            diff.put("category", diff(t.getCategory().getName(), newDeps.category().getName()));
        if (differs(t.getValue(), req.value()))
            diff.put("value", diff(t.getValue(), req.value()));
        if (differs(t.getDate(), req.date()))
            diff.put("date", diff(t.getDate() != null ? t.getDate().toString() : null,
                                  req.date() != null ? req.date().toString() : null));
        if (differs(t.getType(), req.type()))
            diff.put("type", diff(t.getType() != null ? t.getType().getValue() : null,
                                  req.type() != null ? req.type().getValue() : null));
        int oldInstall = t.getInstallmentsNumber() != null ? t.getInstallmentsNumber() : 0;
        int newInstall = req.installmentsNumber() != null ? req.installmentsNumber() : 0;
        if (oldInstall != newInstall)
            diff.put("installmentsNumber", diff(String.valueOf(oldInstall), String.valueOf(newInstall)));
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
