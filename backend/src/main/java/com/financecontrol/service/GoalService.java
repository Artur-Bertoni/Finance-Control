package com.financecontrol.service;

import com.financecontrol.dto.request.GoalRequest;
import com.financecontrol.dto.response.GoalResponse;
import com.financecontrol.entity.Category;
import com.financecontrol.entity.Goal;
import com.financecontrol.entity.TransactionLocale;
import com.financecontrol.enums.GoalStatus;
import com.financecontrol.enums.GoalType;
import com.financecontrol.enums.TransactionType;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.CategoryRepository;
import com.financecontrol.repository.GoalRepository;
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
import java.util.Objects;
import java.util.stream.Collectors;

import static com.financecontrol.service.HistoryService.*;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionLocaleRepository transactionLocaleRepository;
    private final TransactionRepository transactionRepository;
    private final HistoryService historyService;

    @Transactional
    public List<GoalResponse> findAllByUser(Long userId) {
        return goalRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(g -> {
                    double current = calculateCurrentAmount(g);
                    tryAutoComplete(g, current);
                    return GoalResponse.from(g, current);
                })
                .toList();
    }

    @Transactional
    public GoalResponse findById(@NonNull Long id) {
        Goal g = getOrThrow(id);
        double current = calculateCurrentAmount(g);
        tryAutoComplete(g, current);
        return GoalResponse.from(g, current);
    }

    @Transactional
    public GoalResponse create(Long userId,
                               GoalRequest req,
                               boolean force) {
        if (!force && isDuplicateGoal(userId, req))
            throw new BusinessException("error.duplicate.goal");

        Goal goal = buildEntity(userId, req);

        goal.setCreatedAt(LocalDateTime.now());
        goalRepository.save(goal);
        historyService.recordCreation(ENTITY_GOAL, goal.getId(), userId);

        return GoalResponse.from(goal, 0.0);
    }

    @SuppressWarnings("null")
    private boolean isDuplicateGoal(Long userId, GoalRequest req) {
        List<Goal> candidates = goalRepository.findPotentialDuplicates(
                userId, req.name(), req.type(), req.targetAmount(), req.startDate(), req.endDate());
        if (candidates.isEmpty()) return false;

        List<Long> reqCatIds = req.categoryIds() != null ? req.categoryIds().stream().sorted().toList() : List.of();
        List<Long> reqLocIds = req.localeIds()   != null ? req.localeIds().stream().sorted().toList()   : List.of();

        return candidates.stream().anyMatch(g -> {
            List<Long> gCatIds = g.getCategories().stream().map(Category::getId).sorted().toList();
            List<Long> gLocIds = g.getLocales().stream().map(TransactionLocale::getId).sorted().toList();
            return Objects.equals(g.getDescription(),      req.description())
                && Objects.equals(g.getNotifyAt50(),       req.notifyAt50()       != null ? req.notifyAt50()       : true)
                && Objects.equals(g.getNotifyAt75(),       req.notifyAt75()       != null ? req.notifyAt75()       : true)
                && Objects.equals(g.getNotifyAt90(),       req.notifyAt90()       != null ? req.notifyAt90()       : true)
                && Objects.equals(g.getNotifyOnComplete(), req.notifyOnComplete() != null ? req.notifyOnComplete() : true)
                && Objects.equals(g.getNotifyOnDeadline(), req.notifyOnDeadline() != null ? req.notifyOnDeadline() : true)
                && Objects.equals(g.getNotifyOnExceed(),   req.notifyOnExceed()   != null ? req.notifyOnExceed()   : true)
                && gCatIds.equals(reqCatIds)
                && gLocIds.equals(reqLocIds);
        });
    }

    @Transactional
    @SuppressWarnings("null")
    public GoalResponse update(@NonNull Long id,
                                        Long userId,
                                        GoalRequest req) {
        Goal goal = getOrThrow(id);
        Map<String, String[]> diff = buildDiff(goal, req);

        applyRequest(goal, userId, req);
        goalRepository.save(goal);
        historyService.recordChanges(ENTITY_GOAL, id, userId, diff);

        return GoalResponse.from(goal, calculateCurrentAmount(goal));
    }

    @Transactional
    public void archive(@NonNull Long id) {
        Goal goal = getOrThrow(id);
        Map<String, String[]> diff = new LinkedHashMap<>();

        diff.put("status", diff(goal.getStatus().getValue(), GoalStatus.ARCHIVED.getValue()));
        goal.setStatus(GoalStatus.ARCHIVED);
        goalRepository.save(goal);

        historyService.recordChanges(ENTITY_GOAL, id, goal.getUserId(), diff);
    }

    @Transactional
    public void delete(@NonNull Long id) {
        getOrThrow(id);
        goalRepository.deleteById(id);
    }

    @SuppressWarnings("null")
    public double calculateCurrentAmount(Goal goal) {
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

    private void tryAutoComplete(Goal g,
                                 double current) {
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

    Goal getOrThrow(@NonNull Long id) {
        return goalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("error.notFound.goal"));
    }

    private Goal buildEntity(Long userId,
                             GoalRequest req) {
        Goal goal = new Goal();

        goal.setStatus(GoalStatus.ACTIVE);
        applyRequest(goal, userId, req);

        return goal;
    }

    @SuppressWarnings("null")
    private Map<String, String[]> buildDiff(Goal goal,
                                            GoalRequest req) {
        Map<String, String[]> diff = new LinkedHashMap<>();

        if (differs(goal.getName(), req.name()))
            diff.put("name", diff(goal.getName(), req.name()));
        if (differs(goal.getDescription(), req.description()))
            diff.put("description", diff(goal.getDescription(), req.description()));
        if (differs(goal.getType(), req.type()))
            diff.put("type", diff(goal.getType() != null ? goal.getType().getValue() : null, req.type() != null ? req.type().getValue() : null));
        if (differs(goal.getTargetAmount(), req.targetAmount()))
            diff.put("targetAmount", diff(goal.getTargetAmount(), req.targetAmount()));
        if (differs(goal.getStartDate(), req.startDate()))
            diff.put("startDate", diff(goal.getStartDate() != null ? goal.getStartDate().toString() : null, req.startDate() != null ? req.startDate().toString() : null));
        if (differs(goal.getEndDate(), req.endDate()))
            diff.put("endDate", diff(goal.getEndDate() != null ? goal.getEndDate().toString() : null, req.endDate() != null ? req.endDate().toString() : null));

        String oldCats = goal.getCategories().stream().map(Category::getName).sorted().collect(Collectors.joining(", "));
        List<Long> newCatIds = req.categoryIds() != null ? req.categoryIds() : List.of();

        if (!goal.getCategories().stream().map(Category::getId).sorted().toList().equals(newCatIds.stream().sorted().toList())) {
            String newCats = categoryRepository.findAllById(newCatIds).stream().map(Category::getName).sorted().collect(Collectors.joining(", "));
            diff.put("categories", diff(oldCats, newCats));
        }

        String oldLocs = goal.getLocales().stream().map(TransactionLocale::getName).sorted().collect(Collectors.joining(", "));
        List<Long> newLocIds = req.localeIds() != null ? req.localeIds() : List.of();

        if (!goal.getLocales().stream().map(TransactionLocale::getId).sorted().toList().equals(newLocIds.stream().sorted().toList())) {
            String newLocs = transactionLocaleRepository.findAllById(newLocIds).stream().map(TransactionLocale::getName).sorted().collect(Collectors.joining(", "));
            diff.put("locales", diff(oldLocs, newLocs));
        }

        if (differs(goal.getNotifyAt50(), req.notifyAt50() != null ? req.notifyAt50() : Boolean.TRUE))
            diff.put("notifyAt50", diff(String.valueOf(goal.getNotifyAt50()), String.valueOf(req.notifyAt50() != null ? req.notifyAt50() : Boolean.TRUE)));
        if (differs(goal.getNotifyAt75(), req.notifyAt75() != null ? req.notifyAt75() : Boolean.TRUE))
            diff.put("notifyAt75", diff(String.valueOf(goal.getNotifyAt75()), String.valueOf(req.notifyAt75() != null ? req.notifyAt75() : Boolean.TRUE)));
        if (differs(goal.getNotifyAt90(), req.notifyAt90() != null ? req.notifyAt90() : Boolean.TRUE))
            diff.put("notifyAt90", diff(String.valueOf(goal.getNotifyAt90()), String.valueOf(req.notifyAt90() != null ? req.notifyAt90() : Boolean.TRUE)));
        if (differs(goal.getNotifyOnComplete(), req.notifyOnComplete() != null ? req.notifyOnComplete() : Boolean.TRUE))
            diff.put("notifyOnComplete", diff(String.valueOf(goal.getNotifyOnComplete()), String.valueOf(req.notifyOnComplete() != null ? req.notifyOnComplete() : Boolean.TRUE)));
        if (differs(goal.getNotifyOnDeadline(), req.notifyOnDeadline() != null ? req.notifyOnDeadline() : Boolean.TRUE))
            diff.put("notifyOnDeadline", diff(String.valueOf(goal.getNotifyOnDeadline()), String.valueOf(req.notifyOnDeadline() != null ? req.notifyOnDeadline() : Boolean.TRUE)));
        if (differs(goal.getNotifyOnExceed(), req.notifyOnExceed() != null ? req.notifyOnExceed() : Boolean.TRUE))
            diff.put("notifyOnExceed", diff(String.valueOf(goal.getNotifyOnExceed()), String.valueOf(req.notifyOnExceed() != null ? req.notifyOnExceed() : Boolean.TRUE)));

        return diff;
    }

    @SuppressWarnings("null")
    private void applyRequest(Goal goal,
                              Long userId,
                              GoalRequest req) {
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
              .map(lid -> transactionLocaleRepository.findById(lid)
                      .orElseThrow(() -> new ResourceNotFoundException("error.notFound.transactionLocale")))
              .forEach(goal.getLocales()::add);
    }
}
