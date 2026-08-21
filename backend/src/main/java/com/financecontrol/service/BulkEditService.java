package com.financecontrol.service;

import com.financecontrol.dto.request.*;
import com.financecontrol.dto.response.BulkEditResponse;
import com.financecontrol.entity.*;
import com.financecontrol.enums.BulkEntityType;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.repository.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BulkEditService {

    private static final Map<BulkEntityType, Set<String>> EDITABLE_FIELDS = Map.of(
            BulkEntityType.TRANSACTIONS, Set.of("accountId", "categoryId", "transactionLocaleId", "value", "date", "transactionType", "obs"),
            BulkEntityType.CATEGORIES, Set.of("description", "iconKey"),
            BulkEntityType.ACCOUNTS, Set.of("financialInstitutionId", "accountType", "contact", "description", "iconKey"),
            BulkEntityType.FINANCIAL_INSTITUTIONS, Set.of("address", "contact", "iconKey"),
            BulkEntityType.TRANSACTION_LOCALES, Set.of("address", "iconKey"),
            BulkEntityType.GOALS, Set.of("goalType", "targetAmount", "startDate", "endDate",
                    "notifyAt50", "notifyAt75", "notifyAt90", "notifyOnComplete", "notifyOnDeadline", "notifyOnExceed"),
            BulkEntityType.BUDGETS, Set.of("monthlyLimit")
    );

    private final BulkOwnershipResolver ownershipResolver;
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

    @Transactional
    public BulkEditResponse edit(@NonNull BulkEntityType type,
                                 List<Long> ids,
                                 Set<String> fields,
                                 BulkEditValues values,
                                 @NonNull Long userId) {
        Set<String> selected = validateFields(type, fields);
        BulkEditValues safeValues = values != null ? values : emptyValues();

        List<Long> owned = ownershipResolver.ownedIds(type, ids, userId);
        int requested = ids == null ? 0 : ids.size();

        int edited = switch (type) {
            case TRANSACTIONS           -> editTransactions(owned, selected, safeValues, userId);
            case ACCOUNTS               -> editAccounts(owned, selected, safeValues, userId);
            case CATEGORIES             -> editCategories(owned, selected, safeValues, userId);
            case TRANSACTION_LOCALES    -> editLocales(owned, selected, safeValues, userId);
            case FINANCIAL_INSTITUTIONS -> editInstitutions(owned, selected, safeValues, userId);
            case GOALS                  -> editGoals(owned, selected, safeValues, userId);
            case BUDGETS                -> editBudgets(owned, selected, safeValues, userId);
        };

        return new BulkEditResponse(requested, edited, Math.max(requested - edited, 0));
    }

    private Set<String> validateFields(@NonNull BulkEntityType type,
                                       Set<String> fields) {
        if (fields == null || fields.isEmpty())
            throw new BusinessException("error.bulkEdit.noFields");

        Set<String> allowed = EDITABLE_FIELDS.get(type);
        for (String field : fields) {
            if (!allowed.contains(field))
                throw new BusinessException("error.bulkEdit.invalidField");
        }
        return fields;
    }

    private int editTransactions(List<Long> ids,
                                 Set<String> fields,
                                 BulkEditValues values,
                                 @NonNull Long userId) {
        Map<Long, List<Transaction>> groups = groupTransactions(ids);
        int edited = 0;

        for (List<Transaction> group : groups.values()) {
            Transaction target = group.getFirst();
            if (!transactionRepository.existsById(target.getId())) continue;

            transactionService.update(target.getId(), userId, transactionRequestFor(group, fields, values));
            edited += group.size();
        }
        return edited;
    }

    private Map<Long, List<Transaction>> groupTransactions(List<Long> ids) {
        Map<Long, List<Transaction>> groups = new LinkedHashMap<>();

        for (Transaction t : transactionRepository.findAllById(ids)) {
            Long groupId = t.getInstallmentGroupId();
            Long key = groupId != null && !groupId.equals(0L) ? groupId : t.getId();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }

        for (Map.Entry<Long, List<Transaction>> entry : groups.entrySet()) {
            List<Transaction> members = entry.getValue();
            if (members.size() > 1 || isInstallment(members.getFirst()))
                members.sort(Comparator.comparing(t -> Optional.ofNullable(t.getInstallmentIndex()).orElse(0)));
        }
        return groups;
    }

    private TransactionRequest transactionRequestFor(List<Transaction> group,
                                                     Set<String> fields,
                                                     BulkEditValues values) {
        Transaction target = group.getFirst();
        boolean installment = isInstallment(target);

        List<Transaction> fullGroup = installment
                ? transactionRepository.findByInstallmentGroupIdOrderByInstallmentIndexAsc(target.getInstallmentGroupId())
                : List.of(target);

        Transaction first = fullGroup.getFirst();
        double currentValue = installment
                ? Math.round(fullGroup.stream().mapToDouble(Transaction::getValue).sum() * 100) / 100.0
                : target.getValue();

        return new TransactionRequest(
                has(fields, "accountId") ? values.accountId() : idOf(target.getAccount()),
                has(fields, "categoryId") ? values.categoryId() : idOf(target.getCategory()),
                has(fields, "transactionLocaleId") ? values.transactionLocaleId() : idOf(target.getTransactionLocale()),
                has(fields, "value") ? values.value() : currentValue,
                has(fields, "date") ? values.date() : first.getDate(),
                has(fields, "transactionType") ? values.transactionType() : target.getType(),
                installment ? Integer.valueOf(fullGroup.size()) : target.getInstallmentsNumber(),
                has(fields, "obs") ? values.obs() : target.getObs(),
                target.getTransferPartnerId(),
                target.getInvoiceReference()
        );
    }

    private int editAccounts(List<Long> ids,
                             Set<String> fields,
                             BulkEditValues values,
                             @NonNull Long userId) {
        int edited = 0;
        for (Account a : accountRepository.findAllById(ids)) {
            accountService.update(a.getId(), userId, new AccountRequest(
                    has(fields, "financialInstitutionId") ? values.financialInstitutionId() : idOf(a.getFinancialInstitution()),
                    a.getName(),
                    has(fields, "contact") ? values.contact() : a.getContact(),
                    has(fields, "description") ? values.description() : a.getDescription(),
                    a.getBalance(),
                    has(fields, "iconKey") ? values.iconKey() : a.getIconKey(),
                    has(fields, "accountType") ? values.accountType() : a.getType(),
                    a.getClosingDay(),
                    a.getDueDay(),
                    a.getCreditLimit()
            ));
            edited++;
        }
        return edited;
    }

    private int editCategories(List<Long> ids,
                               Set<String> fields,
                               BulkEditValues values,
                               @NonNull Long userId) {
        int edited = 0;
        for (Category c : categoryRepository.findAllById(ids)) {
            categoryService.update(c.getId(), userId, new CategoryRequest(
                    c.getName(),
                    has(fields, "description") ? values.description() : c.getDescription(),
                    has(fields, "iconKey") ? values.iconKey() : c.getIconKey(),
                    c.getAliases().stream().map(CategoryAlias::getAliasName).toList()
            ));
            edited++;
        }
        return edited;
    }

    private int editLocales(List<Long> ids,
                            Set<String> fields,
                            BulkEditValues values,
                            @NonNull Long userId) {
        int edited = 0;
        for (TransactionLocale l : transactionLocaleRepository.findAllById(ids)) {
            transactionLocaleService.update(l.getId(), userId, new TransactionLocaleRequest(
                    l.getName(),
                    has(fields, "address") ? values.address() : l.getAddress(),
                    has(fields, "iconKey") ? values.iconKey() : l.getIconKey()
            ));
            edited++;
        }
        return edited;
    }

    private int editInstitutions(List<Long> ids,
                                 Set<String> fields,
                                 BulkEditValues values,
                                 @NonNull Long userId) {
        int edited = 0;
        for (FinancialInstitution fi : financialInstitutionRepository.findAllById(ids)) {
            financialInstitutionService.update(fi.getId(), userId, new FinancialInstitutionRequest(
                    fi.getName(),
                    has(fields, "address") ? values.address() : fi.getAddress(),
                    has(fields, "contact") ? values.contact() : fi.getContact(),
                    has(fields, "iconKey") ? values.iconKey() : fi.getIconKey()
            ));
            edited++;
        }
        return edited;
    }

    private int editGoals(List<Long> ids,
                          Set<String> fields,
                          BulkEditValues values,
                          @NonNull Long userId) {
        int edited = 0;
        for (Goal g : goalRepository.findAllById(ids)) {
            goalService.update(g.getId(), userId, new GoalRequest(
                    g.getName(),
                    g.getDescription(),
                    has(fields, "goalType") ? values.goalType() : g.getType(),
                    has(fields, "targetAmount") ? values.targetAmount() : g.getTargetAmount(),
                    has(fields, "startDate") ? values.startDate() : g.getStartDate(),
                    has(fields, "endDate") ? values.endDate() : g.getEndDate(),
                    g.getCategories().stream().map(Category::getId).toList(),
                    g.getLocales().stream().map(TransactionLocale::getId).toList(),
                    has(fields, "notifyAt50") ? values.notifyAt50() : g.getNotifyAt50(),
                    has(fields, "notifyAt75") ? values.notifyAt75() : g.getNotifyAt75(),
                    has(fields, "notifyAt90") ? values.notifyAt90() : g.getNotifyAt90(),
                    has(fields, "notifyOnComplete") ? values.notifyOnComplete() : g.getNotifyOnComplete(),
                    has(fields, "notifyOnDeadline") ? values.notifyOnDeadline() : g.getNotifyOnDeadline(),
                    has(fields, "notifyOnExceed") ? values.notifyOnExceed() : g.getNotifyOnExceed()
            ));
            edited++;
        }
        return edited;
    }

    private int editBudgets(List<Long> ids,
                            Set<String> fields,
                            BulkEditValues values,
                            @NonNull Long userId) {
        if (!has(fields, "monthlyLimit")) return 0;

        int edited = 0;
        for (Budget b : budgetRepository.findAllById(ids)) {
            Category category = b.getCategory();
            if (category == null) continue;
            budgetService.upsert(userId, new BudgetRequest(category.getId(), values.monthlyLimit()));
            edited++;
        }
        return edited;
    }

    private boolean isInstallment(Transaction t) {
        Long groupId = t.getInstallmentGroupId();
        return groupId != null && !groupId.equals(0L);
    }

    private Long idOf(Account account) {
        return account != null ? account.getId() : null;
    }

    private Long idOf(Category category) {
        return category != null ? category.getId() : null;
    }

    private Long idOf(TransactionLocale locale) {
        return locale != null ? locale.getId() : null;
    }

    private Long idOf(FinancialInstitution institution) {
        return institution != null ? institution.getId() : null;
    }

    private boolean has(Set<String> fields, String field) {
        return fields.contains(field);
    }

    private BulkEditValues emptyValues() {
        return new BulkEditValues(null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
}
