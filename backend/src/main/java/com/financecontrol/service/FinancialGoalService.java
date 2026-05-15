package com.financecontrol.service;

import com.financecontrol.dto.request.FinancialGoalRequest;
import com.financecontrol.dto.response.FinancialGoalResponse;
import com.financecontrol.entity.Category;
import com.financecontrol.entity.FinancialGoal;
import com.financecontrol.entity.TransactionLocale;
import com.financecontrol.enums.GoalStatus;
import com.financecontrol.enums.GoalType;
import com.financecontrol.enums.TransactionType;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.CategoryRepository;
import com.financecontrol.repository.FinancialGoalRepository;
import com.financecontrol.repository.TransactionLocaleRepository;
import com.financecontrol.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialGoalService {

    private final FinancialGoalRepository goalRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionLocaleRepository localeRepository;
    private final TransactionRepository transactionRepository;

    public List<FinancialGoalResponse> findAllByUser(Long userId) {
        return goalRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(g -> FinancialGoalResponse.from(g, calculateCurrentAmount(g)))
                .toList();
    }

    public FinancialGoalResponse findById(@NonNull Long id) {
        FinancialGoal g = getOrThrow(id);
        return FinancialGoalResponse.from(g, calculateCurrentAmount(g));
    }

    @Transactional
    public FinancialGoalResponse create(Long userId, FinancialGoalRequest req) {
        FinancialGoal goal = buildEntity(userId, req);
        goal.setCreatedAt(LocalDateTime.now());
        goalRepository.save(goal);
        return FinancialGoalResponse.from(goal, 0.0);
    }

    @Transactional
    @SuppressWarnings("null")
    public FinancialGoalResponse update(@NonNull Long id, Long userId, FinancialGoalRequest req) {
        FinancialGoal goal = getOrThrow(id);
        applyRequest(goal, userId, req);
        goalRepository.save(goal);
        return FinancialGoalResponse.from(goal, calculateCurrentAmount(goal));
    }

    @Transactional
    public void archive(@NonNull Long id) {
        FinancialGoal goal = getOrThrow(id);
        goal.setStatus(GoalStatus.ARCHIVED);
        goalRepository.save(goal);
    }

    @Transactional
    public void delete(@NonNull Long id) {
        getOrThrow(id);
        goalRepository.deleteById(id);
    }

    public double calculateCurrentAmount(FinancialGoal goal) {
        TransactionType txType = goal.getType() == GoalType.EXPENSE_LIMIT
                ? TransactionType.DEBIT : TransactionType.CREDIT;
        List<Long> catIds  = goal.getCategories().stream().map(Category::getId).toList();
        List<Long> locIds  = goal.getLocales().stream().map(TransactionLocale::getId).toList();
        LocalDate  endDate = goal.getEndDate().isBefore(LocalDate.now()) ? goal.getEndDate() : LocalDate.now();

        Double result;
        if (catIds.isEmpty() && locIds.isEmpty()) {
            result = transactionRepository.sumForGoal(goal.getUserId(), goal.getStartDate(), endDate, txType);
        } else if (!catIds.isEmpty() && locIds.isEmpty()) {
            result = transactionRepository.sumForGoalByCategories(goal.getUserId(), goal.getStartDate(), endDate, txType, catIds);
        } else if (catIds.isEmpty()) {
            result = transactionRepository.sumForGoalByLocales(goal.getUserId(), goal.getStartDate(), endDate, txType, locIds);
        } else {
            result = transactionRepository.sumForGoalByCategoriesAndLocales(goal.getUserId(), goal.getStartDate(), endDate, txType, catIds, locIds);
        }
        return result != null ? result : 0.0;
    }

    FinancialGoal getOrThrow(@NonNull Long id) {
        return goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound.goal"));
    }

    private FinancialGoal buildEntity(Long userId, FinancialGoalRequest req) {
        FinancialGoal goal = new FinancialGoal();
        goal.setStatus(GoalStatus.ACTIVE);
        applyRequest(goal, userId, req);
        return goal;
    }

    @SuppressWarnings("null")
    private void applyRequest(FinancialGoal goal, Long userId, FinancialGoalRequest req) {
        goal.setUserId(userId);
        goal.setName(req.name());
        goal.setDescription(req.description());
        goal.setType(req.type());
        goal.setTargetAmount(req.targetAmount());
        goal.setStartDate(req.startDate());
        goal.setEndDate(req.endDate());
        goal.setNotifyAt50(req.notifyAt50()       != null ? req.notifyAt50()       : true);
        goal.setNotifyAt75(req.notifyAt75()       != null ? req.notifyAt75()       : true);
        goal.setNotifyAt90(req.notifyAt90()       != null ? req.notifyAt90()       : true);
        goal.setNotifyOnComplete(req.notifyOnComplete() != null ? req.notifyOnComplete() : true);
        goal.setNotifyOnDeadline(req.notifyOnDeadline() != null ? req.notifyOnDeadline() : true);
        goal.setNotifyOnExceed(req.notifyOnExceed()   != null ? req.notifyOnExceed()   : true);

        List<Long> catIds = req.categoryIds() != null ? req.categoryIds() : List.of();
        List<Long> locIds = req.localeIds()   != null ? req.localeIds()   : List.of();

        goal.getCategories().clear();
        catIds.stream()
              .map(cid -> categoryRepository.findById(cid)
                      .orElseThrow(() -> new ResourceNotFoundException("error.notFound.category")))
              .forEach(goal.getCategories()::add);

        goal.getLocales().clear();
        locIds.stream()
              .map(lid -> localeRepository.findById(lid)
                      .orElseThrow(() -> new ResourceNotFoundException("error.notFound.transactionLocale")))
              .forEach(goal.getLocales()::add);
    }
}
