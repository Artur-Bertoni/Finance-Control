package com.financecontrol.service;

import com.financecontrol.dto.response.BulkDeletePreviewResponse;
import com.financecontrol.dto.response.BulkDeleteResponse;
import com.financecontrol.entity.*;
import com.financecontrol.enums.BulkDeleteType;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.repository.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class BulkDeleteService {

    private static final int MAX_IDS = 500;

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionLocaleRepository transactionLocaleRepository;
    private final FinancialInstitutionRepository financialInstitutionRepository;
    private final GoalRepository goalRepository;
    private final BudgetRepository budgetRepository;

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final TransactionLocaleService transactionLocaleService;
    private final FinancialInstitutionService financialInstitutionService;
    private final GoalService goalService;
    private final BudgetService budgetService;

    @Transactional(readOnly = true)
    public BulkDeletePreviewResponse preview(@NonNull BulkDeleteType type,
                                             List<Long> ids,
                                             @NonNull Long userId) {
        List<Long> owned = ownedIds(type, ids, userId);
        if (owned.isEmpty())
            return BulkDeletePreviewResponse.empty();

        int n = owned.size();
        return switch (type) {
            case TRANSACTIONS           -> new BulkDeletePreviewResponse(n, expandTransactionIds(owned).size(), 0, 0);
            case ACCOUNTS               -> new BulkDeletePreviewResponse(n, transactionRepository.countByAccountIdIn(owned), 0, 0);
            case CATEGORIES             -> new BulkDeletePreviewResponse(n, transactionRepository.countByCategoryIdIn(owned), 0, 0);
            case TRANSACTION_LOCALES    -> new BulkDeletePreviewResponse(n, 0, 0, transactionRepository.countByTransactionLocaleIdIn(owned));
            case FINANCIAL_INSTITUTIONS -> new BulkDeletePreviewResponse(n, 0, accountRepository.findIdsByFinancialInstitutionIdIn(owned).size(), 0);
            case GOALS, BUDGETS         -> BulkDeletePreviewResponse.plain(n);
        };
    }

    @Transactional
    public BulkDeleteResponse delete(@NonNull BulkDeleteType type,
                                     List<Long> ids,
                                     @NonNull Long userId) {
        List<Long> owned = ownedIds(type, ids, userId);
        int deleted = 0;

        for (Long id : owned) {
            if (id == null) continue;
            if (BulkDeleteType.TRANSACTIONS == type && !transactionRepository.existsById(id)) continue;
            deleteOne(type, id, userId);
            deleted++;
        }

        int requested = ids == null ? 0 : ids.size();
        return new BulkDeleteResponse(requested, deleted, requested - deleted);
    }

    private void deleteOne(@NonNull BulkDeleteType type,
                           @NonNull Long id,
                           @NonNull Long userId) {
        switch (type) {
            case TRANSACTIONS           -> transactionService.delete(id, userId);
            case ACCOUNTS               -> accountService.delete(id, userId);
            case CATEGORIES             -> categoryService.delete(id, userId);
            case TRANSACTION_LOCALES    -> transactionLocaleService.delete(id, userId);
            case FINANCIAL_INSTITUTIONS -> financialInstitutionService.delete(id, userId);
            case GOALS                  -> goalService.delete(id, userId);
            case BUDGETS                -> budgetService.delete(userId, id);
        }
    }

    private Set<Long> expandTransactionIds(List<Long> ids) {
        Set<Long>  expanded = new LinkedHashSet<>(ids);
        List<Long> groupIds = new ArrayList<>();

        for (Transaction t : transactionRepository.findAllById(ids)) {
            Long groupId = t.getInstallmentGroupId();
            if (groupId != null && !groupId.equals(0L)) groupIds.add(groupId);

            Long partnerId = t.getTransferPartnerId();
            if (partnerId != null && !partnerId.equals(0L)) expanded.add(partnerId);
        }
        if (!groupIds.isEmpty())
            expanded.addAll(transactionRepository.findIdsByInstallmentGroupIdIn(groupIds));

        return expanded;
    }

    private List<Long> ownedIds(@NonNull BulkDeleteType type,
                                List<Long> ids,
                                @NonNull Long userId) {
        if (ids == null || ids.isEmpty()) return List.of();
        if (ids.size() > MAX_IDS) throw new BusinessException("error.bulkDelete.tooManyItems");

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
