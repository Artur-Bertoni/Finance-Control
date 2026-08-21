package com.financecontrol.service;

import com.financecontrol.entity.*;
import com.financecontrol.enums.BulkEntityType;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.repository.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class BulkOwnershipResolver {

    static final int MAX_IDS = 500;

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionLocaleRepository transactionLocaleRepository;
    private final FinancialInstitutionRepository financialInstitutionRepository;
    private final GoalRepository goalRepository;
    private final BudgetRepository budgetRepository;

    public List<Long> ownedIds(@NonNull BulkEntityType type,
                               List<Long> ids,
                               @NonNull Long userId) {
        if (ids == null || ids.isEmpty()) return List.of();
        if (ids.size() > MAX_IDS) throw new BusinessException("error.bulk.tooManyItems");

        List<Long> distinct = ids.stream().filter(Objects::nonNull).distinct().toList();

        return switch (type) {
            case TRANSACTIONS           -> filterOwned(transactionRepository.findAllById(distinct), Transaction::getId, Transaction::getUserId, userId);
            case ACCOUNTS               -> filterOwned(accountRepository.findAllById(distinct), Account::getId, Account::getUserId, userId);
            case CATEGORIES             -> filterOwned(categoryRepository.findAllById(distinct), Category::getId, Category::getUserId, userId);
            case TRANSACTION_LOCALES    -> filterOwned(transactionLocaleRepository.findAllById(distinct), TransactionLocale::getId, TransactionLocale::getUserId, userId);
            case FINANCIAL_INSTITUTIONS -> filterOwned(financialInstitutionRepository.findAllById(distinct), FinancialInstitution::getId, FinancialInstitution::getUserId, userId);
            case GOALS                  -> filterOwned(goalRepository.findAllById(distinct), Goal::getId, Goal::getUserId, userId);
            case BUDGETS                -> filterOwned(budgetRepository.findAllById(distinct), Budget::getId, Budget::getUserId, userId);
        };
    }

    private <T> List<Long> filterOwned(List<T> entities,
                                       Function<T, Long> idFn,
                                       Function<T, Long> userIdFn,
                                       @NonNull Long userId) {
        return entities.stream()
                .filter(e -> userId.equals(userIdFn.apply(e)))
                .map(idFn)
                .filter(Objects::nonNull)
                .toList();
    }
}
