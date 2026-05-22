package com.financecontrol.service;

import com.financecontrol.dto.response.AchievementResponse;
import com.financecontrol.entity.UserAchievement;
import com.financecontrol.enums.AchievementType;
import com.financecontrol.enums.GoalStatus;
import com.financecontrol.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final GoalRepository goalRepository;
    private final TransactionRepository transactionRepository;

    private record Meta(String tier, String iconKey) {}

    private static final Map<AchievementType, Meta> METADATA = Map.ofEntries(
        Map.entry(AchievementType.FIRST_ACCOUNT,                  new Meta("bronze", "ph-bank")),
        Map.entry(AchievementType.FIRST_TRANSACTION,              new Meta("bronze", "ph-receipt")),
        Map.entry(AchievementType.FIRST_GOAL,                     new Meta("bronze", "ph-target")),
        Map.entry(AchievementType.FIRST_CUSTOM_CATEGORY,          new Meta("bronze", "ph-tag")),
        Map.entry(AchievementType.GOAL_COMPLETED,                 new Meta("silver", "ph-check-circle")),
        Map.entry(AchievementType.FIVE_GOALS_COMPLETED,           new Meta("silver", "ph-medal")),
        Map.entry(AchievementType.TEN_GOALS_COMPLETED,            new Meta("gold",   "ph-trophy")),
        Map.entry(AchievementType.GOAL_EARLY_COMPLETION,          new Meta("gold",   "ph-lightning")),
        Map.entry(AchievementType.TRANSACTIONS_7_DIFF_DAYS,       new Meta("silver", "ph-calendar-check")),
        Map.entry(AchievementType.TRANSACTIONS_WEEKLY_MONTH,      new Meta("silver", "ph-calendar-dots")),
        Map.entry(AchievementType.TRANSACTIONS_MONTHLY_YEAR,      new Meta("gold",   "ph-shooting-star")),
        Map.entry(AchievementType.ALL_TRANSACTIONS_CATEGORIZED,   new Meta("silver", "ph-list-checks")),
        Map.entry(AchievementType.STATEMENT_IMPORTED,             new Meta("bronze", "ph-file-arrow-up")),
        Map.entry(AchievementType.THREE_INSTITUTIONS,             new Meta("silver", "ph-buildings")),
        Map.entry(AchievementType.FIVE_CATEGORIES_USED,           new Meta("silver", "ph-grid-four"))
    );

    @Transactional
    public List<AchievementResponse> checkAndList(Long userId) {
        Set<AchievementType> already = achievementRepository.earnedSet(userId);
        List<AchievementType> toAward = new ArrayList<>();

        if (!already.contains(AchievementType.FIRST_ACCOUNT) && accountRepository.countByUserId(userId) >= 1)
            toAward.add(AchievementType.FIRST_ACCOUNT);

        if (!already.contains(AchievementType.FIRST_TRANSACTION) && transactionRepository.countByUserId(userId) >= 1)
            toAward.add(AchievementType.FIRST_TRANSACTION);

        if (!already.contains(AchievementType.FIRST_GOAL) && goalRepository.countByUserId(userId) >= 1)
            toAward.add(AchievementType.FIRST_GOAL);

        if (!already.contains(AchievementType.FIRST_CUSTOM_CATEGORY) && categoryRepository.countByUserId(userId) >= 1)
            toAward.add(AchievementType.FIRST_CUSTOM_CATEGORY);

        long completed = goalRepository.countByUserIdAndStatus(userId, GoalStatus.COMPLETED);
        if (!already.contains(AchievementType.GOAL_COMPLETED) && completed >= 1)
            toAward.add(AchievementType.GOAL_COMPLETED);
        if (!already.contains(AchievementType.FIVE_GOALS_COMPLETED) && completed >= 5)
            toAward.add(AchievementType.FIVE_GOALS_COMPLETED);
        if (!already.contains(AchievementType.TEN_GOALS_COMPLETED) && completed >= 10)
            toAward.add(AchievementType.TEN_GOALS_COMPLETED);

        if (!already.contains(AchievementType.GOAL_EARLY_COMPLETION) &&
                goalRepository.existsByUserIdAndStatusAndEndDateAfter(userId, GoalStatus.COMPLETED, LocalDate.now()))
            toAward.add(AchievementType.GOAL_EARLY_COMPLETION);

        if (!already.contains(AchievementType.TRANSACTIONS_7_DIFF_DAYS) &&
                transactionRepository.countDistinctDatesSince(userId, LocalDate.now().minusDays(30)) >= 7)
            toAward.add(AchievementType.TRANSACTIONS_7_DIFF_DAYS);

        if (!already.contains(AchievementType.TRANSACTIONS_WEEKLY_MONTH) &&
                transactionRepository.countDistinctWeeksSince(userId, LocalDate.now().minusDays(28)) >= 4)
            toAward.add(AchievementType.TRANSACTIONS_WEEKLY_MONTH);

        if (!already.contains(AchievementType.TRANSACTIONS_MONTHLY_YEAR) &&
                transactionRepository.countDistinctMonthsSince(userId, LocalDate.now().minusMonths(11).withDayOfMonth(1)) >= 12)
            toAward.add(AchievementType.TRANSACTIONS_MONTHLY_YEAR);

        if (!already.contains(AchievementType.ALL_TRANSACTIONS_CATEGORIZED)) {
            LocalDate start = LocalDate.now().withDayOfMonth(1);
            LocalDate end   = start.plusMonths(1).minusDays(1);
            if (transactionRepository.countByUserId(userId) > 0 &&
                    transactionRepository.countUncategorizedInPeriod(userId, start, end) == 0)
                toAward.add(AchievementType.ALL_TRANSACTIONS_CATEGORIZED);
        }

        if (!already.contains(AchievementType.STATEMENT_IMPORTED)) {
            Long maxOnDay = transactionRepository.maxTransactionsOnSameDate(userId);
            if (maxOnDay != null && maxOnDay >= 5)
                toAward.add(AchievementType.STATEMENT_IMPORTED);
        }

        if (!already.contains(AchievementType.THREE_INSTITUTIONS) &&
                accountRepository.countDistinctInstitutionsByUserId(userId) >= 3)
            toAward.add(AchievementType.THREE_INSTITUTIONS);

        if (!already.contains(AchievementType.FIVE_CATEGORIES_USED) &&
                transactionRepository.countDistinctCategoriesUsed(userId) >= 5)
            toAward.add(AchievementType.FIVE_CATEGORIES_USED);

        for (AchievementType type : toAward) {
            achievementRepository.save(new UserAchievement(userId, type));
            already.add(type);
        }

        Map<AchievementType, UserAchievement> earnedMap = new HashMap<>();
        achievementRepository.findByUserId(userId).forEach(ua -> earnedMap.put(ua.getAchievementType(), ua));

        return Arrays.stream(AchievementType.values()).map(type -> {
            Meta meta     = METADATA.get(type);
            UserAchievement ua = earnedMap.get(type);
            return new AchievementResponse(
                    type.name(),
                    meta.tier(),
                    meta.iconKey(),
                    ua != null,
                    ua != null ? ua.getEarnedAt() : null
            );
        }).toList();
    }
}
