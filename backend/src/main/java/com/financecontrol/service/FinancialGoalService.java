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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.financecontrol.service.ChangeHistoryService.*;

@Service
@RequiredArgsConstructor
public class FinancialGoalService {

    private final FinancialGoalRepository goalRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionLocaleRepository localeRepository;
    private final TransactionRepository transactionRepository;
    private final ChangeHistoryService changeHistoryService;

    @Transactional
    public List<FinancialGoalResponse> findAllByUser(Long userId) {
        return goalRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(g -> {
                    double current = calculateCurrentAmount(g);
                    tryAutoComplete(g, current);
                    return FinancialGoalResponse.from(g, current);
                })
                .toList();
    }

    @Transactional
    public FinancialGoalResponse findById(@NonNull Long id) {
        FinancialGoal g = getOrThrow(id);
        double current = calculateCurrentAmount(g);
        tryAutoComplete(g, current);
        return FinancialGoalResponse.from(g, current);
    }

    @Transactional
    public FinancialGoalResponse create(Long userId, FinancialGoalRequest req) {
        FinancialGoal goal = buildEntity(userId, req);
        goal.setCreatedAt(LocalDateTime.now());
        goalRepository.save(goal);
        changeHistoryService.recordCreation(ENTITY_GOAL, goal.getId(), userId);
        return FinancialGoalResponse.from(goal, 0.0);
    }

    @Transactional
    @SuppressWarnings("null")
    public FinancialGoalResponse update(@NonNull Long id, Long userId, FinancialGoalRequest req) {
        FinancialGoal goal = getOrThrow(id);
        Map<String, String[]> diff = buildDiff(goal, req);
        applyRequest(goal, userId, req);
        goalRepository.save(goal);
        changeHistoryService.recordChanges(ENTITY_GOAL, id, userId, diff);
        return FinancialGoalResponse.from(goal, calculateCurrentAmount(goal));
    }

    @Transactional
    public void archive(@NonNull Long id) {
        FinancialGoal goal = getOrThrow(id);
        Map<String, String[]> diff = new LinkedHashMap<>();
        diff.put("status", diff(goal.getStatus().getValue(), GoalStatus.ARCHIVED.getValue()));
        goal.setStatus(GoalStatus.ARCHIVED);
        goalRepository.save(goal);
        changeHistoryService.recordChanges(ENTITY_GOAL, id, goal.getUserId(), diff);
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
        LocalDate  endDate = goal.getEndDate() != null && goal.getEndDate().isBefore(LocalDate.now())
                ? goal.getEndDate() : LocalDate.now();

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

    private void tryAutoComplete(FinancialGoal g, double current) {
        if (g.getStatus() != GoalStatus.ACTIVE) return;
        boolean shouldComplete = switch (g.getType()) {
            case SAVINGS, INCOME -> current >= g.getTargetAmount();
            case EXPENSE_LIMIT   -> false;
        };
        if (shouldComplete) {
            g.setStatus(GoalStatus.COMPLETED);
            goalRepository.save(g);
        }
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

    private Map<String, String[]> buildDiff(FinancialGoal g, FinancialGoalRequest req) {
        Map<String, String[]> diff = new LinkedHashMap<>();
        if (differs(g.getName(), req.name()))
            diff.put("name", diff(g.getName(), req.name()));
        if (differs(g.getDescription(), req.description()))
            diff.put("description", diff(g.getDescription(), req.description()));
        if (differs(g.getType(), req.type()))
            diff.put("type", diff(g.getType() != null ? g.getType().getValue() : null,
                                  req.type() != null ? req.type().getValue() : null));
        if (differs(g.getTargetAmount(), req.targetAmount()))
            diff.put("targetAmount", diff(g.getTargetAmount(), req.targetAmount()));
        if (differs(g.getStartDate(), req.startDate()))
            diff.put("startDate", diff(g.getStartDate() != null ? g.getStartDate().toString() : null,
                                       req.startDate() != null ? req.startDate().toString() : null));
        if (differs(g.getEndDate(), req.endDate()))
            diff.put("endDate", diff(g.getEndDate() != null ? g.getEndDate().toString() : null,
                                     req.endDate() != null ? req.endDate().toString() : null));

        String oldCats = g.getCategories().stream().map(Category::getName).sorted().collect(Collectors.joining(", "));
        List<Long> newCatIds = req.categoryIds() != null ? req.categoryIds() : List.of();
        if (!g.getCategories().stream().map(Category::getId).sorted().toList()
                .equals(newCatIds.stream().sorted().toList())) {
            String newCats = categoryRepository.findAllById(newCatIds).stream()
                    .map(Category::getName).sorted().collect(Collectors.joining(", "));
            diff.put("categories", diff(oldCats, newCats));
        }

        String oldLocs = g.getLocales().stream().map(TransactionLocale::getName).sorted().collect(Collectors.joining(", "));
        List<Long> newLocIds = req.localeIds() != null ? req.localeIds() : List.of();
        if (!g.getLocales().stream().map(TransactionLocale::getId).sorted().toList()
                .equals(newLocIds.stream().sorted().toList())) {
            String newLocs = localeRepository.findAllById(newLocIds).stream()
                    .map(TransactionLocale::getName).sorted().collect(Collectors.joining(", "));
            diff.put("locales", diff(oldLocs, newLocs));
        }

        if (differs(g.getNotifyAt50(), req.notifyAt50() != null ? req.notifyAt50() : Boolean.TRUE))
            diff.put("notifyAt50", diff(String.valueOf(g.getNotifyAt50()), String.valueOf(req.notifyAt50() != null ? req.notifyAt50() : Boolean.TRUE)));
        if (differs(g.getNotifyAt75(), req.notifyAt75() != null ? req.notifyAt75() : Boolean.TRUE))
            diff.put("notifyAt75", diff(String.valueOf(g.getNotifyAt75()), String.valueOf(req.notifyAt75() != null ? req.notifyAt75() : Boolean.TRUE)));
        if (differs(g.getNotifyAt90(), req.notifyAt90() != null ? req.notifyAt90() : Boolean.TRUE))
            diff.put("notifyAt90", diff(String.valueOf(g.getNotifyAt90()), String.valueOf(req.notifyAt90() != null ? req.notifyAt90() : Boolean.TRUE)));
        if (differs(g.getNotifyOnComplete(), req.notifyOnComplete() != null ? req.notifyOnComplete() : Boolean.TRUE))
            diff.put("notifyOnComplete", diff(String.valueOf(g.getNotifyOnComplete()), String.valueOf(req.notifyOnComplete() != null ? req.notifyOnComplete() : Boolean.TRUE)));
        if (differs(g.getNotifyOnDeadline(), req.notifyOnDeadline() != null ? req.notifyOnDeadline() : Boolean.TRUE))
            diff.put("notifyOnDeadline", diff(String.valueOf(g.getNotifyOnDeadline()), String.valueOf(req.notifyOnDeadline() != null ? req.notifyOnDeadline() : Boolean.TRUE)));
        if (differs(g.getNotifyOnExceed(), req.notifyOnExceed() != null ? req.notifyOnExceed() : Boolean.TRUE))
            diff.put("notifyOnExceed", diff(String.valueOf(g.getNotifyOnExceed()), String.valueOf(req.notifyOnExceed() != null ? req.notifyOnExceed() : Boolean.TRUE)));
        return diff;
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
