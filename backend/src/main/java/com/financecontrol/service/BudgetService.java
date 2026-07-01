package com.financecontrol.service;

import com.financecontrol.dto.request.BudgetRequest;
import com.financecontrol.dto.response.BudgetResponse;
import com.financecontrol.entity.Budget;
import com.financecontrol.entity.Category;
import com.financecontrol.enums.TransactionType;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.BudgetRepository;
import com.financecontrol.repository.CategoryRepository;
import com.financecontrol.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class BudgetService {

    private static final String CATEGORY_NOT_FOUND = "error.notFound.category";
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<BudgetResponse> findAllByUser(Long userId) {
        LocalDate start = LocalDate.now(ZONE).withDayOfMonth(1);
        LocalDate end   = YearMonth.now(ZONE).atEndOfMonth();
        return budgetRepository.findByUserId(userId).stream()
                .map(b -> toResponse(b, userId, start, end))
                .sorted(Comparator.comparing(BudgetResponse::categoryName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public BudgetResponse upsert(Long userId, BudgetRequest req) {
        Long categoryId = req.categoryId();
        if (categoryId == null)
            throw new ResourceNotFoundException(CATEGORY_NOT_FOUND);
        if (req.monthlyLimit() == null || req.monthlyLimit() <= 0)
            throw new BusinessException("error.budget.invalidLimit");

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND));
        if (!category.getUserId().equals(userId))
            throw new ResourceNotFoundException(CATEGORY_NOT_FOUND);

        Budget budget = budgetRepository.findByUserIdAndCategory_Id(userId, categoryId)
                .orElseGet(() -> {
                    Budget b = new Budget();
                    b.setUserId(userId);
                    b.setCategory(category);
                    b.setCreatedAt(LocalDateTime.now(ZONE));
                    return b;
                });
        budget.setMonthlyLimit(req.monthlyLimit());
        Budget saved = budgetRepository.save(budget);

        LocalDate start = LocalDate.now(ZONE).withDayOfMonth(1);
        LocalDate end   = YearMonth.now(ZONE).atEndOfMonth();
        return toResponse(saved, userId, start, end);
    }

    @Transactional
    public void delete(Long userId, @NonNull Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound.budget"));
        if (!budget.getUserId().equals(userId))
            throw new ResourceNotFoundException("error.notFound.budget");
        budgetRepository.deleteById(id);
    }

    private BudgetResponse toResponse(Budget b, Long userId, LocalDate start, LocalDate end) {
        Category category = Objects.requireNonNull(b.getCategory());
        Long categoryId = Objects.requireNonNull(category.getId());
        String name = category.getName();
        String icon = category.getIconKey();
        double spent = transactionRepository.sumForGoalByCategories(
                userId, start, end, TransactionType.DEBIT, List.of(categoryId));
        double limit = b.getMonthlyLimit() != null ? b.getMonthlyLimit() : 0.0;
        double percent = limit > 0 ? (spent / limit) * 100.0 : 0.0;
        return new BudgetResponse(b.getId(), categoryId, name, icon, limit, spent, percent);
    }
}
