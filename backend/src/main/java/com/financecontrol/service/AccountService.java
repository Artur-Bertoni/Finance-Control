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
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final FinancialInstitutionRepository financialInstitutionRepository;

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
        Account account = new Account(null, userId, fi, req.name(), req.contact(), req.description(), req.balance());
        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse update(@NonNull Long id, Long userId, AccountRequest req) {
        Account account = getOrThrow(id);
        FinancialInstitution fi = financialInstitutionRepository.findById(req.financialInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound.financialInstitution"));
        account.setFinancialInstitution(fi);
        account.setName(req.name());
        account.setContact(req.contact());
        account.setDescription(req.description());
        account.setBalance(req.balance());
        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional
    public void delete(@NonNull Long id) {
        getOrThrow(id);
        accountRepository.deleteById(id);
    }

    @Transactional
    public void patchBalance(Long id, Double delta) {
        accountRepository.patchBalance(id, delta);
    }

    Account getOrThrow(@NonNull Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound.account"));
    }
}
