package com.financecontrol.service;

import com.financecontrol.dto.request.AccountRequest;
import com.financecontrol.dto.response.AccountResponse;
import com.financecontrol.entity.Account;
import com.financecontrol.entity.FinancialInstitution;
import com.financecontrol.enums.AccountType;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.AccountRepository;
import com.financecontrol.repository.FinancialInstitutionRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.financecontrol.service.HistoryService.*;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final FinancialInstitutionRepository financialInstitutionRepository;
    private final HistoryService historyService;

    @Transactional(readOnly = true)
    @Cacheable(value = "accounts", key = "#userId")
    public List<AccountResponse> findAllByUser(Long userId) {
        return accountRepository.findByUserIdOrderByNameAsc(userId).stream().map(AccountResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse findById(@NonNull Long id) {
        return AccountResponse.from(getOrThrow(id));
    }

    public Double totalValue(Long userId,
                             Long accountId) {
        return accountRepository.sumBalance(userId, accountId);
    }

    @Transactional
    @CacheEvict(value = "accounts", key = "#userId")
    public AccountResponse create(Long userId,
                                  AccountRequest req,
                                  boolean force) {
        if (!force && accountRepository.existsByUserIdAndNameIgnoreCase(userId, req.name()))
            throw new BusinessException("error.duplicate.name");

        FinancialInstitution fi = financialInstitutionRepository.findById(req.financialInstitutionId()).orElseThrow(() -> new ResourceNotFoundException("error.notFound.financialInstitution"));
        AccountType type = req.type() != null ? req.type() : AccountType.CHECKING;
        Account account = new Account(null, userId, fi, req.name(), req.contact(), req.description(), req.balance(), req.iconKey(),
                type, req.closingDay(), req.dueDay(), LocalDateTime.now(), false);

        AccountResponse result = AccountResponse.from(accountRepository.save(account));

        historyService.recordCreation(ENTITY_ACCOUNT, result.id(), userId);
        return result;
    }

    @Transactional
    @CacheEvict(value = "accounts", key = "#userId")
    public AccountResponse update(@NonNull Long id,
                                  Long userId,
                                  AccountRequest req) {
        Account account = getOrThrow(id);
        FinancialInstitution fi = financialInstitutionRepository.findById(req.financialInstitutionId()).orElseThrow(() -> new ResourceNotFoundException("error.notFound.financialInstitution"));

        Map<String, String[]> diff = buildDiff(account, req, fi);

        account.setFinancialInstitution(fi);
        account.setName(req.name());
        account.setContact(req.contact());
        account.setDescription(req.description());
        account.setBalance(req.balance());
        account.setIconKey(req.iconKey());
        if (req.type() != null) account.setType(req.type());
        account.setClosingDay(req.closingDay());
        account.setDueDay(req.dueDay());

        AccountResponse result = AccountResponse.from(accountRepository.save(account));
        historyService.recordChanges(ENTITY_ACCOUNT, id, userId, diff);
        return result;
    }

    @Transactional
    @CacheEvict(value = "accounts", allEntries = true)
    public void delete(@NonNull Long id) {
        getOrThrow(id);

        accountRepository.deleteById(id);
    }

    @Transactional
    @CacheEvict(value = "accounts", allEntries = true)
    public void patchBalance(@NonNull Long id,
                             Double delta) {
        Account account    = getOrThrow(id);
        double oldBalance  = account.getBalance() != null ? account.getBalance() : 0.0;
        double newBalance  = oldBalance + delta;

        accountRepository.patchBalance(id, delta);

        Map<String, String[]> diff = new LinkedHashMap<>();
        diff.put("balance", diff(String.valueOf(oldBalance), String.valueOf(newBalance)));
        historyService.recordChanges(ENTITY_ACCOUNT, id, account.getUserId(), diff);
    }

    Account getOrThrow(@NonNull Long id) {
        return accountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("error.notFound.account"));
    }

    private Map<String, String[]> buildDiff(Account account,
                                            AccountRequest req,
                                            FinancialInstitution newFi) {
        Map<String, String[]> diff = new LinkedHashMap<>();

        if (differs(account.getName(), req.name()))
            diff.put("name", diff(account.getName(), req.name()));

        String oldFiName = account.getFinancialInstitution() != null ? account.getFinancialInstitution().getName() : null;
        if (differs(account.getFinancialInstitution() != null ? account.getFinancialInstitution().getId() : null, req.financialInstitutionId()))
            diff.put("financialInstitution", diff(oldFiName, newFi.getName()));
        if (differs(account.getContact(), req.contact()))
            diff.put("contact", diff(account.getContact(), req.contact()));
        if (differs(account.getDescription(), req.description()))
            diff.put("description", diff(account.getDescription(), req.description()));
        if (differs(account.getBalance(), req.balance()))
            diff.put("balance", diff(account.getBalance(), req.balance()));
        if (differs(account.getIconKey(), req.iconKey()))
            diff.put("iconKey", diff(account.getIconKey(), req.iconKey()));

        if (req.type() != null && differs(account.getType(), req.type()))
            diff.put("type", diff(account.getType() != null ? account.getType().name() : null, req.type().name()));

        return diff;
    }
}
