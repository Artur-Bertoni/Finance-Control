package com.financecontrol.service;

import com.financecontrol.dto.request.AccountRequest;
import com.financecontrol.dto.response.AccountResponse;
import com.financecontrol.entity.Account;
import com.financecontrol.entity.FinancialInstitution;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.AccountRepository;
import com.financecontrol.repository.FinancialInstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository repository;
    private final FinancialInstitutionRepository fiRepository;

    public List<AccountResponse> findAllByUser(Long userId) {
        return repository.findByUserIdOrderByIdDesc(userId).stream()
                .map(AccountResponse::from).toList();
    }

    public AccountResponse findById(Long id) {
        return AccountResponse.from(getOrThrow(id));
    }

    public Double totalValue(Long userId, Long accountId) {
        return repository.sumBalance(userId, accountId);
    }

    @Transactional
    public AccountResponse create(Long userId, AccountRequest req) {
        FinancialInstitution fi = fiRepository.findById(req.financialInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException("Instituição financeira não encontrada"));
        Account account = new Account(null, userId, fi, req.name(), req.contact(), req.description(), req.balance());
        return AccountResponse.from(repository.save(account));
    }

    @Transactional
    public AccountResponse update(Long id, Long userId, AccountRequest req) {
        Account account = getOrThrow(id);
        FinancialInstitution fi = fiRepository.findById(req.financialInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException("Instituição financeira não encontrada"));
        account.setFinancialInstitution(fi);
        account.setName(req.name());
        account.setContact(req.contact());
        account.setDescription(req.description());
        account.setBalance(req.balance());
        return AccountResponse.from(repository.save(account));
    }

    @Transactional
    public void delete(Long id) {
        getOrThrow(id);
        repository.deleteById(id);
    }

    @Transactional
    public void patchBalance(Long id, Double delta) {
        repository.patchBalance(id, delta);
    }

    Account getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));
    }
}
