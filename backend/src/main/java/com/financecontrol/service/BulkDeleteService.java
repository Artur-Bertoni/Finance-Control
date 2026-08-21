package com.financecontrol.service;

import com.financecontrol.dto.response.BulkDeletePreviewResponse;
import com.financecontrol.dto.response.BulkDeleteResponse;
import com.financecontrol.entity.Transaction;
import com.financecontrol.enums.BulkEntityType;
import com.financecontrol.repository.AccountRepository;
import com.financecontrol.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BulkDeleteService {

    private final BulkOwnershipResolver ownershipResolver;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final TransactionLocaleService transactionLocaleService;
    private final FinancialInstitutionService financialInstitutionService;
    private final GoalService goalService;
    private final BudgetService budgetService;

    @Transactional(readOnly = true)
    public BulkDeletePreviewResponse preview(@NonNull BulkEntityType type,
                                             List<Long> ids,
                                             @NonNull Long userId) {
        List<Long> owned = ownershipResolver.ownedIds(type, ids, userId);
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
    public BulkDeleteResponse delete(@NonNull BulkEntityType type,
                                     List<Long> ids,
                                     @NonNull Long userId) {
        List<Long> owned = ownershipResolver.ownedIds(type, ids, userId);
        int deleted = 0;

        for (Long id : owned) {
            if (id == null) continue;
            if (BulkEntityType.TRANSACTIONS == type && !transactionRepository.existsById(id)) continue;
            deleteOne(type, id, userId);
            deleted++;
        }

        int requested = ids == null ? 0 : ids.size();
        return new BulkDeleteResponse(requested, deleted, requested - deleted);
    }

    private void deleteOne(@NonNull BulkEntityType type,
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
}
