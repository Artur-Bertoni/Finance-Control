package com.financecontrol.service;

import com.financecontrol.dto.request.AccountRequest;
import com.financecontrol.dto.response.AccountResponse;
import com.financecontrol.entity.Account;
import com.financecontrol.entity.FinancialInstitution;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.AccountRepository;
import com.financecontrol.repository.FinancialInstitutionRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.financecontrol.service.ChangeHistoryService.*;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final FinancialInstitutionRepository financialInstitutionRepository;
    private final ChangeHistoryService changeHistoryService;

    public List<AccountResponse> findAllByUser(Long userId) {
        return accountRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(AccountResponse::from).toList();
    }

    public AccountResponse findById(@NonNull Long id) {
        return AccountResponse.from(getOrThrow(id));
    }

    public Double totalValue(Long userId, Long accountId) {
        return accountRepository.sumBalance(userId, accountId);
    }

    @Transactional
    public AccountResponse create(Long userId, AccountRequest req) {
        FinancialInstitution fi = financialInstitutionRepository.findById(req.financialInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound.financialInstitution"));
        Account account = new Account(null, userId, fi, req.name(), req.contact(), req.description(), req.balance(), req.iconKey(), LocalDateTime.now());
        AccountResponse result = AccountResponse.from(accountRepository.save(account));
        changeHistoryService.recordCreation(ENTITY_ACCOUNT, result.id(), userId);
        return result;
    }

    @Transactional
    public AccountResponse update(@NonNull Long id, Long userId, AccountRequest req) {
        Account account = getOrThrow(id);
        FinancialInstitution fi = financialInstitutionRepository.findById(req.financialInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound.financialInstitution"));

        Map<String, String[]> diff = buildDiff(account, req, fi);

        account.setFinancialInstitution(fi);
        account.setName(req.name());
        account.setContact(req.contact());
        account.setDescription(req.description());
        account.setBalance(req.balance());
        account.setIconKey(req.iconKey());

        AccountResponse result = AccountResponse.from(accountRepository.save(account));
        changeHistoryService.recordChanges(ENTITY_ACCOUNT, id, userId, diff);
        return result;
    }

    @Transactional
    public void delete(@NonNull Long id) {
        getOrThrow(id);

        accountRepository.deleteById(id);
    }

    @Transactional
    public void patchBalance(Long id, Double delta) {
        Account account    = getOrThrow(id);
        double oldBalance  = account.getBalance() != null ? account.getBalance() : 0.0;
        double newBalance  = oldBalance + delta;
        accountRepository.patchBalance(id, delta);
        Map<String, String[]> diff = new LinkedHashMap<>();
        diff.put("balance", diff(String.valueOf(oldBalance), String.valueOf(newBalance)));
        changeHistoryService.recordChanges(ENTITY_ACCOUNT, id, account.getUserId(), diff);
    }

    Account getOrThrow(@NonNull Long id) {
        return accountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("error.notFound.account"));
    }

    @SuppressWarnings("null")
    private Map<String, String[]> buildDiff(Account a, AccountRequest req, FinancialInstitution newFi) {
        Map<String, String[]> diff = new LinkedHashMap<>();
        if (differs(a.getName(), req.name()))
            diff.put("name", diff(a.getName(), req.name()));
        String oldFiName = a.getFinancialInstitution() != null ? a.getFinancialInstitution().getName() : null;
        if (differs(a.getFinancialInstitution() != null ? a.getFinancialInstitution().getId() : null, req.financialInstitutionId()))
            diff.put("financialInstitution", diff(oldFiName, newFi.getName()));
        if (differs(a.getContact(), req.contact()))
            diff.put("contact", diff(a.getContact(), req.contact()));
        if (differs(a.getDescription(), req.description()))
            diff.put("description", diff(a.getDescription(), req.description()));
        if (differs(a.getBalance(), req.balance()))
            diff.put("balance", diff(a.getBalance(), req.balance()));
        if (differs(a.getIconKey(), req.iconKey()))
            diff.put("iconKey", diff(a.getIconKey(), req.iconKey()));
        return diff;
    }
}
