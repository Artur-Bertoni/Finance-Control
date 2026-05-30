package com.financecontrol.service;

import com.financecontrol.entity.Goal;
import com.financecontrol.entity.User;
import com.financecontrol.enums.AppNotificationType;
import com.financecontrol.enums.GoalNotificationType;
import com.financecontrol.enums.GoalStatus;
import com.financecontrol.enums.GoalType;
import com.financecontrol.repository.GoalNotificationLogRepository;
import com.financecontrol.repository.GoalRepository;
import com.financecontrol.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class GoalNotificationSchedulerTest {

    @Mock GoalRepository                goalRepository;
    @Mock GoalNotificationLogRepository goalNotificationLogRepository;
    @Mock GoalService                   goalService;
    @Mock EmailService                  emailService;
    @Mock AppNotificationService        appNotificationService;
    @Mock UserRepository                userRepository;

    // Build scheduler manually (constructor takes @Value timezone)
    private GoalNotificationScheduler scheduler() {
        return new GoalNotificationScheduler(goalRepository, goalNotificationLogRepository,
                goalService, emailService, appNotificationService, userRepository, "UTC");
    }

    // ── processGoals – sem metas ativas ──────────────────────────────────────

    @Test
    void processGoals_semMetasAtivas_naoFazNada() {
        when(goalRepository.findByStatus(GoalStatus.ACTIVE)).thenReturn(List.of());

        scheduler().processGoals();

        verifyNoInteractions(goalService, emailService, appNotificationService);
    }

    // ── processGoals – usuário não encontrado ────────────────────────────────

    @Test
    void processGoals_usuarioNaoEncontrado_pulaMeta() {
        Goal goal = activeGoal(1L, 1L, "Meta", GoalType.SAVINGS, 1000.0,
                LocalDate.now().minusDays(1), LocalDate.now().minusDays(1));
        when(goalRepository.findByStatus(GoalStatus.ACTIVE)).thenReturn(List.of(goal));
        when(goalService.calculateCurrentAmount(goal)).thenReturn(500.0);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        scheduler().processGoals();

        verifyNoInteractions(emailService, appNotificationService);
    }

    // ── processGoals – meta expirou + poupança abaixo do alvo → EXPIRED ──────

    @Test
    void processGoals_metaExpirada_poupancaIncompleta_marcaExpired() {
        LocalDate yesterday = LocalDate.now(java.time.ZoneId.of("UTC")).minusDays(1);
        Goal goal = activeGoal(1L, 1L, "Viagem", GoalType.SAVINGS, 1000.0,
                yesterday.minusMonths(1), yesterday);
        goal.setNotifyOnComplete(false);
        when(goalRepository.findByStatus(GoalStatus.ACTIVE)).thenReturn(List.of(goal));
        when(goalService.calculateCurrentAmount(goal)).thenReturn(500.0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWith(1L, "joao")));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduler().processGoals();

        assertThat(goal.getStatus()).isEqualTo(GoalStatus.EXPIRED);
    }

    // ── processGoals – meta expirou + poupança completa → COMPLETED ──────────

    @Test
    void processGoals_metaExpirada_poupancaCompleta_marcaCompleted() {
        LocalDate yesterday = LocalDate.now(java.time.ZoneId.of("UTC")).minusDays(1);
        Goal goal = activeGoal(1L, 1L, "Viagem", GoalType.SAVINGS, 1000.0,
                yesterday.minusMonths(1), yesterday);
        goal.setNotifyOnComplete(true);
        when(goalRepository.findByStatus(GoalStatus.ACTIVE)).thenReturn(List.of(goal));
        when(goalService.calculateCurrentAmount(goal)).thenReturn(1000.0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWith(1L, "joao")));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(goalNotificationLogRepository.existsByGoalIdAndNotificationType(1L, GoalNotificationType.COMPLETED))
                .thenReturn(false);

        scheduler().processGoals();

        assertThat(goal.getStatus()).isEqualTo(GoalStatus.COMPLETED);
        verify(appNotificationService).createGoalNotification(
                eq(1L), eq(1L), eq("Viagem"), eq(AppNotificationType.GOAL_COMPLETED), isNull());
    }

    // ── processGoals – expense limit expirou, ficou abaixo → COMPLETED ───────

    @Test
    void processGoals_expenseLimit_abaixoDoAlvo_marcaCompleted() {
        LocalDate yesterday = LocalDate.now(java.time.ZoneId.of("UTC")).minusDays(1);
        Goal goal = activeGoal(2L, 1L, "Restaurantes", GoalType.EXPENSE_LIMIT, 500.0,
                yesterday.minusMonths(1), yesterday);
        goal.setNotifyOnComplete(true);
        when(goalRepository.findByStatus(GoalStatus.ACTIVE)).thenReturn(List.of(goal));
        when(goalService.calculateCurrentAmount(goal)).thenReturn(300.0); // < 500 = success
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWith(1L, "maria")));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(goalNotificationLogRepository.existsByGoalIdAndNotificationType(2L, GoalNotificationType.COMPLETED))
                .thenReturn(false);

        scheduler().processGoals();

        assertThat(goal.getStatus()).isEqualTo(GoalStatus.COMPLETED);
    }

    // ── processGoals – prazo se aproxima, aviso de deadline ──────────────────

    @Test
    void processGoals_prazoProximo_criaAvisoDeadline() {
        LocalDate today    = LocalDate.now(java.time.ZoneId.of("UTC"));
        LocalDate deadline = today.plusDays(3); // within 7-day window
        Goal goal = activeGoal(3L, 1L, "Carro", GoalType.SAVINGS, 5000.0,
                today.minusMonths(1), deadline);
        goal.setNotifyOnDeadline(true);
        User user = userWith(1L, "joao");
        user.setGoalEmailNotificationEnabled(true);

        when(goalRepository.findByStatus(GoalStatus.ACTIVE)).thenReturn(List.of(goal));
        when(goalService.calculateCurrentAmount(goal)).thenReturn(2000.0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(goalNotificationLogRepository.existsByGoalIdAndNotificationType(3L, GoalNotificationType.DEADLINE_WARNING))
                .thenReturn(false);

        scheduler().processGoals();

        verify(emailService).sendGoalNotification(eq(user), eq(goal),
                eq(GoalNotificationType.DEADLINE_WARNING), eq(2000.0));
        verify(appNotificationService).createGoalNotification(
                eq(1L), eq(3L), eq("Carro"), eq(AppNotificationType.GOAL_DEADLINE_WARNING), isNull());
        verify(goalNotificationLogRepository).save(any());
    }

    // ── processGoals – prazo já notificado, não duplica ──────────────────────

    @Test
    void processGoals_deadlineJaNotificado_naoEnviaDeNovo() {
        LocalDate today    = LocalDate.now(java.time.ZoneId.of("UTC"));
        LocalDate deadline = today.plusDays(3);
        Goal goal = activeGoal(4L, 1L, "Meta", GoalType.SAVINGS, 1000.0,
                today.minusMonths(1), deadline);
        goal.setNotifyOnDeadline(true);

        when(goalRepository.findByStatus(GoalStatus.ACTIVE)).thenReturn(List.of(goal));
        when(goalService.calculateCurrentAmount(goal)).thenReturn(500.0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWith(1L, "joao")));
        when(goalNotificationLogRepository.existsByGoalIdAndNotificationType(4L, GoalNotificationType.DEADLINE_WARNING))
                .thenReturn(true);

        scheduler().processGoals();

        verifyNoInteractions(emailService, appNotificationService);
    }

    // ── processGoals – prazo ainda distante, não envia aviso ─────────────────

    @Test
    void processGoals_prazoAindaDistante_naoEnviaAviso() {
        LocalDate today    = LocalDate.now(java.time.ZoneId.of("UTC"));
        LocalDate deadline = today.plusDays(30); // beyond 7-day window
        Goal goal = activeGoal(5L, 1L, "Meta", GoalType.SAVINGS, 1000.0,
                today.minusMonths(1), deadline);
        goal.setNotifyOnDeadline(true);

        when(goalRepository.findByStatus(GoalStatus.ACTIVE)).thenReturn(List.of(goal));
        when(goalService.calculateCurrentAmount(goal)).thenReturn(500.0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWith(1L, "joao")));

        scheduler().processGoals();

        verifyNoInteractions(emailService, appNotificationService);
    }

    // ── processGoals – exceção em meta individual não interrompe o loop ───────

    @Test
    void processGoals_excecaoEmUmaMeta_continuaProcessandoOutras() {
        Goal bad  = activeGoal(6L, 1L, "Bad",  GoalType.SAVINGS, 100.0, null, null);
        Goal good = activeGoal(7L, 2L, "Good", GoalType.SAVINGS, 100.0, null, null);
        User user2 = userWith(2L, "maria");

        when(goalRepository.findByStatus(GoalStatus.ACTIVE)).thenReturn(List.of(bad, good));
        when(goalService.calculateCurrentAmount(bad)).thenThrow(new RuntimeException("erro"));
        when(goalService.calculateCurrentAmount(good)).thenReturn(50.0);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        // should not throw
        scheduler().processGoals();

        verify(userRepository).findById(2L);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static Goal activeGoal(Long id, Long userId, String name, GoalType type,
                                   Double target, LocalDate start, LocalDate end) {
        Goal g = new Goal();
        g.setId(id);
        g.setUserId(userId);
        g.setName(name);
        g.setType(type);
        g.setStatus(GoalStatus.ACTIVE);
        g.setTargetAmount(target);
        g.setStartDate(start);
        g.setEndDate(end);
        g.setNotifyOnComplete(false);
        g.setNotifyOnDeadline(false);
        g.setNotifyOnExceed(false);
        g.setNotifyAt50(false);
        g.setNotifyAt75(false);
        g.setNotifyAt90(false);
        return g;
    }

    private static User userWith(Long id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(username + "@test.com");
        u.setGoalEmailNotificationEnabled(false);
        return u;
    }
}
