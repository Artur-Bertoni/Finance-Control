package com.financecontrol.service;

import com.financecontrol.dto.request.TransactionRequest;
import com.financecontrol.dto.response.TransactionResponse;
import com.financecontrol.entity.*;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository repository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionLocaleRepository localeRepository;

    public List<TransactionResponse> findAllByUser(Long userId, LocalDate startDate, LocalDate endDate,
                                                    Long categoryId, Long accountId) {
        return repository.findAllFiltered(userId, startDate, endDate, categoryId, accountId)
                .stream().map(TransactionResponse::from).toList();
    }

    public TransactionResponse findById(Long id) {
        return TransactionResponse.from(getOrThrow(id));
    }

    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest req) {
        applyBalanceDelta(req.accountId(), req.type(), req.value());
        Transaction t = buildEntity(userId, req);
        return TransactionResponse.from(repository.save(t));
    }

    @Transactional
    public TransactionResponse update(Long id, Long userId, TransactionRequest req) {
        Transaction existing = getOrThrow(id);

        // revert old balance effect
        String oldType = existing.getType();
        double revert = oldType.equals("credit") ? -existing.getValue() : existing.getValue();
        accountRepository.patchBalance(existing.getAccount().getId(), revert);

        // apply new balance effect
        applyBalanceDelta(req.accountId(), req.type(), req.value());

        // if this is part of a transfer, update the partner too
        if (existing.getTransferPartnerId() != null && existing.getTransferPartnerId() != 0) {
            Transaction partner = getOrThrow(existing.getTransferPartnerId());
            if (!partner.getValue().equals(req.value())) {
                TransactionRequest partnerReq = new TransactionRequest(
                        partner.getAccount().getId(), req.categoryId(), req.transactionLocaleId(),
                        req.value(), req.date(),
                        req.type().equals("debit") ? "credit" : "debit",
                        req.installmentsNumber(), req.obs(), id);
                update(partner.getId(), userId, partnerReq);
            }
        }

        updateEntity(existing, userId, req);
        return TransactionResponse.from(repository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        Transaction t = getOrThrow(id);

        // revert balance
        double revert = t.getType().equals("credit") ? -t.getValue() : t.getValue();
        accountRepository.patchBalance(t.getAccount().getId(), revert);

        // delete transfer partner first if exists
        if (t.getTransferPartnerId() != null && t.getTransferPartnerId() != 0) {
            if (repository.existsById(t.getTransferPartnerId())) {
                delete(t.getTransferPartnerId());
            }
        }

        repository.deleteById(id);
    }

    @Transactional
    public TransactionResponse patchTransferPartner(Long id, Long partnerId) {
        Transaction t = getOrThrow(id);
        t.setTransferPartnerId(partnerId);
        return TransactionResponse.from(repository.save(t));
    }

    Transaction getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada"));
    }

    private void applyBalanceDelta(Long accountId, String type, Double value) {
        double delta = type.equals("credit") ? value : -value;
        accountRepository.patchBalance(accountId, delta);
    }

    private Transaction buildEntity(Long userId, TransactionRequest req) {
        Account account = accountRepository.findById(req.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        TransactionLocale locale = req.transactionLocaleId() != null
                ? localeRepository.findById(req.transactionLocaleId()).orElse(null) : null;

        return new Transaction(null, userId, account, category, locale,
                req.value(), req.date(), req.type(),
                req.installmentsNumber() != null ? req.installmentsNumber() : 0,
                req.obs(),
                req.transferPartnerId() != null ? req.transferPartnerId() : 0L);
    }

    private void updateEntity(Transaction t, Long userId, TransactionRequest req) {
        Account account = accountRepository.findById(req.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        TransactionLocale locale = req.transactionLocaleId() != null
                ? localeRepository.findById(req.transactionLocaleId()).orElse(null) : null;

        t.setUserId(userId);
        t.setAccount(account);
        t.setCategory(category);
        t.setTransactionLocale(locale);
        t.setValue(req.value());
        t.setDate(req.date());
        t.setType(req.type());
        t.setInstallmentsNumber(req.installmentsNumber() != null ? req.installmentsNumber() : 0);
        t.setObs(req.obs());
        t.setTransferPartnerId(req.transferPartnerId() != null ? req.transferPartnerId() : 0L);
    }
}
