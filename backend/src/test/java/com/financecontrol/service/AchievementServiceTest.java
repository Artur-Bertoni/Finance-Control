package com.financecontrol.service;

import com.financecontrol.dto.response.AchievementResponse;
import com.financecontrol.entity.UserAchievement;
import com.financecontrol.enums.AchievementType;
import com.financecontrol.enums.GoalStatus;
import com.financecontrol.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock AchievementRepository  achievementRepository;
    @Mock AccountRepository      accountRepository;
    @Mock CategoryRepository     categoryRepository;
    @Mock GoalRepository         goalRepository;
    @Mock TransactionRepository  transactionRepository;

    @InjectMocks AchievementService achievementService;

    // ── checkAndList – nenhuma conquista ainda desbloqueada ─────────────────

    @Test
    void checkAndList_semNenhumaConquista_retornaListaCompleta() {
        stubNoAchievements();
        stubAllCountsZero();

        List<AchievementResponse> result = achievementService.checkAndList(1L);

        assertThat(result).hasSize(AchievementType.values().length);
        assertThat(result).allMatch(r -> !r.earned());
    }

    // ── FIRST_ACCOUNT ────────────────────────────────────────────────────────

    @Test
    void checkAndList_primeiraContaCriada_desbloqueia_FIRST_ACCOUNT() {
        stubNoAchievements();
        stubAllCountsZero();
        when(accountRepository.countByUserIdAndSeededFalse(1L)).thenReturn(1L);
        when(achievementRepository.save(any(UserAchievement.class))).thenAnswer(inv -> inv.getArgument(0));

        achievementService.checkAndList(1L);

        verify(achievementRepository, atLeastOnce()).save(argThat(
                ua -> ua.getAchievementType() == AchievementType.FIRST_ACCOUNT));
    }

    // ── FIRST_TRANSACTION ────────────────────────────────────────────────────

    @Test
    void checkAndList_primeiraTransacao_desbloqueia_FIRST_TRANSACTION() {
        stubNoAchievements();
        stubAllCountsZero();
        when(transactionRepository.countByUserId(1L)).thenReturn(1L);
        when(achievementRepository.save(any(UserAchievement.class))).thenAnswer(inv -> inv.getArgument(0));

        achievementService.checkAndList(1L);

        verify(achievementRepository, atLeastOnce()).save(argThat(
                ua -> ua.getAchievementType() == AchievementType.FIRST_TRANSACTION));
    }

    // ── FIRST_GOAL ───────────────────────────────────────────────────────────

    @Test
    void checkAndList_primeiraMetaCriada_desbloqueia_FIRST_GOAL() {
        stubNoAchievements();
        stubAllCountsZero();
        when(goalRepository.countByUserId(1L)).thenReturn(1L);
        when(achievementRepository.save(any(UserAchievement.class))).thenAnswer(inv -> inv.getArgument(0));

        achievementService.checkAndList(1L);

        verify(achievementRepository, atLeastOnce()).save(argThat(
                ua -> ua.getAchievementType() == AchievementType.FIRST_GOAL));
    }

    // ── GOAL_COMPLETED ───────────────────────────────────────────────────────

    @Test
    void checkAndList_umaMetaConcluida_desbloqueia_GOAL_COMPLETED() {
        stubNoAchievements();
        stubAllCountsZero();
        when(goalRepository.countByUserIdAndStatus(1L, GoalStatus.COMPLETED)).thenReturn(1L);
        when(achievementRepository.save(any(UserAchievement.class))).thenAnswer(inv -> inv.getArgument(0));

        achievementService.checkAndList(1L);

        verify(achievementRepository, atLeastOnce()).save(argThat(
                ua -> ua.getAchievementType() == AchievementType.GOAL_COMPLETED));
    }

    // ── FIVE_GOALS_COMPLETED ─────────────────────────────────────────────────

    @Test
    void checkAndList_cincoMetasConcluidas_desbloqueia_FIVE_GOALS_COMPLETED() {
        stubNoAchievements();
        stubAllCountsZero();
        when(goalRepository.countByUserIdAndStatus(1L, GoalStatus.COMPLETED)).thenReturn(5L);
        when(achievementRepository.save(any(UserAchievement.class))).thenAnswer(inv -> inv.getArgument(0));

        achievementService.checkAndList(1L);

        verify(achievementRepository, atLeastOnce()).save(argThat(
                ua -> ua.getAchievementType() == AchievementType.FIVE_GOALS_COMPLETED));
    }

    // ── TEN_GOALS_COMPLETED ──────────────────────────────────────────────────

    @Test
    void checkAndList_dezMetasConcluidas_desbloqueia_TEN_GOALS_COMPLETED() {
        stubNoAchievements();
        stubAllCountsZero();
        when(goalRepository.countByUserIdAndStatus(1L, GoalStatus.COMPLETED)).thenReturn(10L);
        when(achievementRepository.save(any(UserAchievement.class))).thenAnswer(inv -> inv.getArgument(0));

        achievementService.checkAndList(1L);

        verify(achievementRepository, atLeastOnce()).save(argThat(
                ua -> ua.getAchievementType() == AchievementType.TEN_GOALS_COMPLETED));
    }

    // ── GOAL_EARLY_COMPLETION ────────────────────────────────────────────────

    @Test
    void checkAndList_metaConcluidaAntesDoFim_desbloqueia_GOAL_EARLY_COMPLETION() {
        stubNoAchievements();
        stubAllCountsZero();
        when(goalRepository.existsByUserIdAndStatusAndEndDateAfter(
                eq(1L), eq(GoalStatus.COMPLETED), any(LocalDate.class))).thenReturn(true);
        when(achievementRepository.save(any(UserAchievement.class))).thenAnswer(inv -> inv.getArgument(0));

        achievementService.checkAndList(1L);

        verify(achievementRepository, atLeastOnce()).save(argThat(
                ua -> ua.getAchievementType() == AchievementType.GOAL_EARLY_COMPLETION));
    }

    // ── STATEMENT_IMPORTED ───────────────────────────────────────────────────

    @Test
    void checkAndList_cincoTransacoesNomesmoDia_desbloqueia_STATEMENT_IMPORTED() {
        stubNoAchievements();
        stubAllCountsZero();
        when(transactionRepository.maxTransactionsOnSameDate(1L)).thenReturn(5L);
        when(achievementRepository.save(any(UserAchievement.class))).thenAnswer(inv -> inv.getArgument(0));

        achievementService.checkAndList(1L);

        verify(achievementRepository, atLeastOnce()).save(argThat(
                ua -> ua.getAchievementType() == AchievementType.STATEMENT_IMPORTED));
    }

    // ── THREE_INSTITUTIONS ───────────────────────────────────────────────────

    @Test
    void checkAndList_tresInstituicoes_desbloqueia_THREE_INSTITUTIONS() {
        stubNoAchievements();
        stubAllCountsZero();
        when(accountRepository.countDistinctInstitutionsByUserId(1L)).thenReturn(3L);
        when(achievementRepository.save(any(UserAchievement.class))).thenAnswer(inv -> inv.getArgument(0));

        achievementService.checkAndList(1L);

        verify(achievementRepository, atLeastOnce()).save(argThat(
                ua -> ua.getAchievementType() == AchievementType.THREE_INSTITUTIONS));
    }

    // ── conquista já obtida não duplica ──────────────────────────────────────

    @Test
    void checkAndList_conquistaJaObtida_naoSalvaNovamente() {
        UserAchievement ua = new UserAchievement(1L, AchievementType.FIRST_ACCOUNT);
        when(achievementRepository.findByUserId(1L)).thenReturn(List.of(ua));
        when(achievementRepository.earnedSet(1L))
                .thenReturn(new java.util.HashSet<>(java.util.Set.of(AchievementType.FIRST_ACCOUNT)));
        stubAllCountsZero();
        when(accountRepository.countByUserIdAndSeededFalse(1L)).thenReturn(1L);

        achievementService.checkAndList(1L);

        verify(achievementRepository, never()).save(argThat(
                u -> u.getAchievementType() == AchievementType.FIRST_ACCOUNT));
    }

    // ── ALL_TRANSACTIONS_CATEGORIZED ─────────────────────────────────────────

    @Test
    void checkAndList_todasCategorizadas_desbloqueia_ALL_TRANSACTIONS_CATEGORIZED() {
        stubNoAchievements();
        stubAllCountsZero();
        when(transactionRepository.countByUserId(1L)).thenReturn(5L);
        when(transactionRepository.countUncategorizedInPeriod(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(0L);
        when(achievementRepository.save(any(UserAchievement.class))).thenAnswer(inv -> inv.getArgument(0));

        achievementService.checkAndList(1L);

        verify(achievementRepository, atLeastOnce()).save(argThat(
                u -> u.getAchievementType() == AchievementType.ALL_TRANSACTIONS_CATEGORIZED));
    }

    // ── FIVE_CATEGORIES_USED ─────────────────────────────────────────────────

    @Test
    void checkAndList_cincoCategorias_desbloqueia_FIVE_CATEGORIES_USED() {
        stubNoAchievements();
        stubAllCountsZero();
        when(transactionRepository.countDistinctCategoriesUsed(1L)).thenReturn(5L);
        when(achievementRepository.save(any(UserAchievement.class))).thenAnswer(inv -> inv.getArgument(0));

        achievementService.checkAndList(1L);

        verify(achievementRepository, atLeastOnce()).save(argThat(
                u -> u.getAchievementType() == AchievementType.FIVE_CATEGORIES_USED));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void stubNoAchievements() {
        when(achievementRepository.findByUserId(1L)).thenReturn(List.of());
        when(achievementRepository.earnedSet(1L)).thenReturn(new java.util.HashSet<>());
    }

    private void stubAllCountsZero() {
        when(accountRepository.countByUserIdAndSeededFalse(1L)).thenReturn(0L);
        when(transactionRepository.countByUserId(1L)).thenReturn(0L);
        when(goalRepository.countByUserId(1L)).thenReturn(0L);
        when(categoryRepository.countByUserIdAndSeededFalse(1L)).thenReturn(0L);
        when(goalRepository.countByUserIdAndStatus(1L, GoalStatus.COMPLETED)).thenReturn(0L);
        when(goalRepository.existsByUserIdAndStatusAndEndDateAfter(
                eq(1L), eq(GoalStatus.COMPLETED), any(LocalDate.class))).thenReturn(false);
        when(transactionRepository.countDistinctDatesSince(eq(1L), any(LocalDate.class))).thenReturn(0L);
        when(transactionRepository.countDistinctWeeksSince(eq(1L), any(LocalDate.class))).thenReturn(0L);
        when(transactionRepository.countDistinctMonthsSince(eq(1L), any(LocalDate.class))).thenReturn(0L);
        when(transactionRepository.countUncategorizedInPeriod(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(0L);
        when(transactionRepository.maxTransactionsOnSameDate(1L)).thenReturn(null);
        when(accountRepository.countDistinctInstitutionsByUserId(1L)).thenReturn(0L);
        when(transactionRepository.countDistinctCategoriesUsed(1L)).thenReturn(0L);
    }
}
