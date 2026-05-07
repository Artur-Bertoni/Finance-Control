package com.financecontrol.service;

import com.financecontrol.dto.request.TransactionRequest;
import com.financecontrol.dto.response.TransactionResponse;
import com.financecontrol.entity.*;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final String TYPE_CREDIT = "credit";
    private static final String TYPE_DEBIT  = "debit";

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionLocaleRepository transactionLocaleRepository;

    public List<TransactionResponse> findAllByUser(Long userId, LocalDate startDate, LocalDate endDate,
                                                    Long categoryId, Long accountId) {
        return transactionRepository.findAllFiltered(userId, startDate, endDate, categoryId, accountId)
                .stream().map(TransactionResponse::from).toList();
    }

    public TransactionResponse findById(@NonNull Long id) {
        return TransactionResponse.from(getOrThrow(id));
    }

    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest req) {
        applyBalanceDelta(req.accountId(), req.type(), req.value());
        return TransactionResponse.from(transactionRepository.save(buildEntity(userId, req)));
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
    Transaction getOrThrow(@NonNull Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound.transaction"));
    }

    private TransactionResponse updateOne(@NonNull Long id, Long userId, TransactionRequest req) {
        Transaction existing = getOrThrow(id);

        String oldType = existing.getType();
        double revert = TYPE_CREDIT.equals(oldType) ? -existing.getValue() : existing.getValue();
        Long accountId = existing.getAccount().getId();
        if (accountId != null) {
            accountRepository.patchBalance(accountId, revert);
        }
        applyBalanceDelta(req.accountId(), req.type(), req.value());

        Long partnerId = existing.getTransferPartnerId();
        if (isTransferPartner(partnerId)) {
            Transaction partner = getOrThrow(partnerId);
            if (!partner.getValue().equals(req.value())) {
                String partnerType = TYPE_DEBIT.equals(req.type()) ? TYPE_CREDIT : TYPE_DEBIT;
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

        updateEntity(existing, userId, req);
        return TransactionResponse.from(transactionRepository.save(existing));
    }

    private void deleteOne(@NonNull Long id) {
        Transaction t = getOrThrow(id);
        double revert = TYPE_CREDIT.equals(t.getType()) ? -t.getValue() : t.getValue();
        Long accountId = t.getAccount().getId();
        if (accountId != null) {
            accountRepository.patchBalance(accountId, revert);
        }

        Long partnerId = t.getTransferPartnerId();
        if (isTransferPartner(partnerId) && transactionRepository.existsById(partnerId)) {
            deleteOne(partnerId);
        }

        transactionRepository.deleteById(id);
    }

    private void applyBalanceDelta(@NonNull Long accountId, String type, Double value) {
        double delta = TYPE_CREDIT.equals(type) ? value : -value;
        accountRepository.patchBalance(accountId, delta);
    }

    private boolean isTransferPartner(Long partnerId) {
        return partnerId != null && !partnerId.equals(0L);
    }

    private record TransactionDeps(Account account, Category category, TransactionLocale locale) {}

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
                req.value(), req.date(), req.type(),
                installmentsNumber,
                req.obs(),
                transferPartnerId);
    }

    private void updateEntity(Transaction t, Long userId, TransactionRequest req) {
        TransactionDeps deps = loadDeps(req);
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
}
