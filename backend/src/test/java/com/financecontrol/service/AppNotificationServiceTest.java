package com.financecontrol.service;

import com.financecontrol.dto.response.AppNotificationResponse;
import com.financecontrol.entity.AppNotification;
import com.financecontrol.entity.Goal;
import com.financecontrol.enums.AppNotificationType;
import com.financecontrol.enums.GoalNotificationType;
import com.financecontrol.enums.GoalStatus;
import com.financecontrol.enums.GoalType;
import com.financecontrol.repository.AppNotificationRepository;
import com.financecontrol.repository.GoalNotificationLogRepository;
import com.financecontrol.repository.GoalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class AppNotificationServiceTest {

    @Mock AppNotificationRepository      appNotificationRepository;
    @Mock GoalRepository                 goalRepository;
    @Mock GoalNotificationLogRepository  goalNotificationLogRepository;
    @Mock GoalService                    goalService;

    @InjectMocks AppNotificationService appNotificationService;

    // ── checkGoalImpact – sem metas ativas ───────────────────────────────────

    @Test
    void checkGoalImpact_semMetasAtivas_retornaListaVazia() {
        when(goalRepository.findByUserIdAndStatus(1L, GoalStatus.ACTIVE)).thenReturn(List.of());

        List<AppNotificationResponse> result = appNotificationService.checkGoalImpact(1L, 10L);

        assertThat(result).isEmpty();
        verifyNoInteractions(appNotificationRepository);
    }

    // ── checkGoalImpact – alvo zero ignorado ─────────────────────────────────

    @Test
    void checkGoalImpact_targetZero_ignoraMeta() {
        Goal goal = savingsGoal(1L, 1L, "Meta Zero", 0.0);
        when(goalRepository.findByUserIdAndStatus(1L, GoalStatus.ACTIVE)).thenReturn(List.of(goal));

        List<AppNotificationResponse> result = appNotificationService.checkGoalImpact(1L, 10L);

        assertThat(result).isEmpty();
        verifyNoInteractions(goalNotificationLogRepository);
    }

    // ── checkGoalImpact – 100% poupança → GOAL_COMPLETED ────────────────────

    @Test
    void checkGoalImpact_cem_porcento_poupanca_criaNotificacao_GOAL_COMPLETED() {
        Goal goal = savingsGoal(1L, 1L, "Viagem", 1000.0);
        goal.setNotifyOnComplete(true);

        when(goalRepository.findByUserIdAndStatus(1L, GoalStatus.ACTIVE)).thenReturn(List.of(goal));
        when(goalService.calculateCurrentAmount(goal)).thenReturn(1000.0);
        when(goalNotificationLogRepository.existsByGoalIdAndNotificationType(1L, GoalNotificationType.COMPLETED))
                .thenReturn(false);
        AppNotification saved = notification(5L, 1L, AppNotificationType.GOAL_COMPLETED);
        when(appNotificationRepository.save(any(AppNotification.class))).thenReturn(saved);

        List<AppNotificationResponse> result = appNotificationService.checkGoalImpact(1L, 10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(AppNotificationType.GOAL_COMPLETED);
        verify(goalNotificationLogRepository).save(any());
    }

    // ── checkGoalImpact – já notificado → não duplica ────────────────────────

    @Test
    void checkGoalImpact_jaNotificado_naoduplicaNotificacao() {
        Goal goal = savingsGoal(1L, 1L, "Viagem", 1000.0);
        goal.setNotifyOnComplete(true);

        when(goalRepository.findByUserIdAndStatus(1L, GoalStatus.ACTIVE)).thenReturn(List.of(goal));
        when(goalService.calculateCurrentAmount(goal)).thenReturn(1000.0);
        when(goalNotificationLogRepository.existsByGoalIdAndNotificationType(1L, GoalNotificationType.COMPLETED))
                .thenReturn(true);

        List<AppNotificationResponse> result = appNotificationService.checkGoalImpact(1L, 10L);

        assertThat(result).isEmpty();
        verify(appNotificationRepository, never()).save(any());
    }

    // ── checkGoalImpact – EXPENSE_LIMIT 100% → GOAL_EXCEEDED ─────────────────

    @Test
    void checkGoalImpact_expenseLimit_excedido_criaNotificacao_GOAL_EXCEEDED() {
        Goal goal = expenseLimitGoal(2L, 1L, "Restaurantes", 500.0);
        goal.setNotifyOnExceed(true);

        when(goalRepository.findByUserIdAndStatus(1L, GoalStatus.ACTIVE)).thenReturn(List.of(goal));
        when(goalService.calculateCurrentAmount(goal)).thenReturn(500.0);
        when(goalNotificationLogRepository.existsByGoalIdAndNotificationType(2L, GoalNotificationType.EXCEEDED))
                .thenReturn(false);
        AppNotification saved = notification(6L, 1L, AppNotificationType.GOAL_EXCEEDED);
        when(appNotificationRepository.save(any(AppNotification.class))).thenReturn(saved);

        List<AppNotificationResponse> result = appNotificationService.checkGoalImpact(1L, 20L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(AppNotificationType.GOAL_EXCEEDED);
    }

    // ── checkGoalImpact – milestone 90% ──────────────────────────────────────

    @Test
    void checkGoalImpact_noventa_porcento_criaNotificacao_MILESTONE_90() {
        Goal goal = savingsGoal(3L, 1L, "Carro", 10000.0);
        goal.setNotifyAt90(true);
        goal.setNotifyOnComplete(false);

        when(goalRepository.findByUserIdAndStatus(1L, GoalStatus.ACTIVE)).thenReturn(List.of(goal));
        when(goalService.calculateCurrentAmount(goal)).thenReturn(9000.0);
        when(goalNotificationLogRepository.existsByGoalIdAndNotificationType(3L, GoalNotificationType.MILESTONE_90))
                .thenReturn(false);
        AppNotification saved = notification(7L, 1L, AppNotificationType.GOAL_MILESTONE_90);
        when(appNotificationRepository.save(any(AppNotification.class))).thenReturn(saved);

        List<AppNotificationResponse> result = appNotificationService.checkGoalImpact(1L, 30L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(AppNotificationType.GOAL_MILESTONE_90);
    }

    // ── checkGoalImpact – milestone 75% ──────────────────────────────────────

    @Test
    void checkGoalImpact_setentaECinco_porcento_criaNotificacao_MILESTONE_75() {
        Goal goal = savingsGoal(4L, 1L, "Reforma", 1000.0);
        goal.setNotifyAt75(true);

        when(goalRepository.findByUserIdAndStatus(1L, GoalStatus.ACTIVE)).thenReturn(List.of(goal));
        when(goalService.calculateCurrentAmount(goal)).thenReturn(800.0); // 80%
        when(goalNotificationLogRepository.existsByGoalIdAndNotificationType(4L, GoalNotificationType.MILESTONE_75))
                .thenReturn(false);
        AppNotification saved = notification(8L, 1L, AppNotificationType.GOAL_MILESTONE_75);
        when(appNotificationRepository.save(any(AppNotification.class))).thenReturn(saved);

        List<AppNotificationResponse> result = appNotificationService.checkGoalImpact(1L, 40L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(AppNotificationType.GOAL_MILESTONE_75);
    }

    // ── checkGoalImpact – milestone 50% ──────────────────────────────────────

    @Test
    void checkGoalImpact_cinquenta_porcento_criaNotificacao_MILESTONE_50() {
        Goal goal = savingsGoal(5L, 1L, "Notebook", 1000.0);
        goal.setNotifyAt50(true);

        when(goalRepository.findByUserIdAndStatus(1L, GoalStatus.ACTIVE)).thenReturn(List.of(goal));
        when(goalService.calculateCurrentAmount(goal)).thenReturn(550.0); // 55%
        when(goalNotificationLogRepository.existsByGoalIdAndNotificationType(5L, GoalNotificationType.MILESTONE_50))
                .thenReturn(false);
        AppNotification saved = notification(9L, 1L, AppNotificationType.GOAL_MILESTONE_50);
        when(appNotificationRepository.save(any(AppNotification.class))).thenReturn(saved);

        List<AppNotificationResponse> result = appNotificationService.checkGoalImpact(1L, 50L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(AppNotificationType.GOAL_MILESTONE_50);
    }

    // ── checkGoalImpact – múltiplos milestones em uma única transação ────────

    @Test
    void checkGoalImpact_cem_porcento_todosFlags_criaTodasNotificacoes() {
        Goal goal = savingsGoal(6L, 1L, "Casa", 1000.0);
        goal.setNotifyAt50(true);
        goal.setNotifyAt75(true);
        goal.setNotifyAt90(true);
        goal.setNotifyOnComplete(true);

        when(goalRepository.findByUserIdAndStatus(1L, GoalStatus.ACTIVE)).thenReturn(List.of(goal));
        when(goalService.calculateCurrentAmount(goal)).thenReturn(1000.0); // 100%
        when(goalNotificationLogRepository.existsByGoalIdAndNotificationType(eq(6L), any()))
                .thenReturn(false);
        when(appNotificationRepository.save(any(AppNotification.class)))
                .thenReturn(notification(60L, 1L, AppNotificationType.GOAL_COMPLETED));

        List<AppNotificationResponse> result = appNotificationService.checkGoalImpact(1L, 60L);

        // completed + 90 + 75 + 50 = 4
        assertThat(result).hasSize(4);
        verify(goalNotificationLogRepository, times(4)).save(any());
    }

    // ── checkGoalImpact – expense limit abaixo do limite não notifica ────────

    @Test
    void checkGoalImpact_expenseLimit_abaixoDoLimite_naoNotifica() {
        Goal goal = expenseLimitGoal(7L, 1L, "Lazer", 1000.0);
        goal.setNotifyAt50(true);
        goal.setNotifyOnExceed(true);

        when(goalRepository.findByUserIdAndStatus(1L, GoalStatus.ACTIVE)).thenReturn(List.of(goal));
        when(goalService.calculateCurrentAmount(goal)).thenReturn(300.0); // 30%

        List<AppNotificationResponse> result = appNotificationService.checkGoalImpact(1L, 70L);

        assertThat(result).isEmpty();
        verify(appNotificationRepository, never()).save(any());
    }

    // ── createGoalNotification ────────────────────────────────────────────────

    @Test
    void createGoalNotification_salvaERetornaResponse() {
        AppNotification saved = notification(99L, 1L, AppNotificationType.GOAL_COMPLETED);
        when(appNotificationRepository.save(any(AppNotification.class))).thenReturn(saved);

        AppNotificationResponse result = appNotificationService.createGoalNotification(
                1L, 1L, "Meta", AppNotificationType.GOAL_COMPLETED, null);

        assertThat(result.type()).isEqualTo(AppNotificationType.GOAL_COMPLETED);
        assertThat(result.id()).isEqualTo(99L);
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    void findAll_retornaNotificacoesDoUsuario() {
        AppNotification n = notification(1L, 1L, AppNotificationType.GOAL_COMPLETED);
        when(appNotificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(n));

        List<AppNotificationResponse> result = appNotificationService.findAll(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(AppNotificationType.GOAL_COMPLETED);
    }

    // ── getUnreadCount ────────────────────────────────────────────────────────

    @Test
    void getUnreadCount_retornaContagem() {
        when(appNotificationRepository.countByUserIdAndReadFalse(1L)).thenReturn(3L);
        assertThat(appNotificationService.getUnreadCount(1L)).isEqualTo(3L);
    }

    // ── markAsRead ────────────────────────────────────────────────────────────

    @Test
    void markAsRead_pertenceAoUsuario_marcaComoLida() {
        AppNotification n = notification(1L, 1L, AppNotificationType.GOAL_COMPLETED);
        when(appNotificationRepository.findById(1L)).thenReturn(Optional.of(n));
        when(appNotificationRepository.save(any(AppNotification.class))).thenAnswer(inv -> inv.getArgument(0));

        appNotificationService.markAsRead(1L, 1L);

        assertThat(n.isRead()).isTrue();
        verify(appNotificationRepository).save(n);
    }

    @Test
    void markAsRead_pertenceAOutroUsuario_naoAltera() {
        AppNotification n = notification(1L, 2L, AppNotificationType.GOAL_COMPLETED); // userId=2
        when(appNotificationRepository.findById(1L)).thenReturn(Optional.of(n));

        appNotificationService.markAsRead(1L, 1L); // caller userId=1

        assertThat(n.isRead()).isFalse();
        verify(appNotificationRepository, never()).save(any());
    }

    @Test
    void markAsRead_notificacaoNaoEncontrada_naoLancaExcecao() {
        when(appNotificationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatCode(() -> appNotificationService.markAsRead(1L, 99L)).doesNotThrowAnyException();
    }

    // ── markAllAsRead ─────────────────────────────────────────────────────────

    @Test
    void markAllAsRead_chamaBulkUpdate() {
        appNotificationService.markAllAsRead(1L);
        verify(appNotificationRepository).markAllAsReadByUserId(1L);
    }

    // ── saveUserAction ────────────────────────────────────────────────────────

    @Test
    void saveUserAction_salvaComSeveridadeInfo() {
        AppNotification n = new AppNotification();
        n.setId(10L);
        n.setUserId(1L);
        n.setType(AppNotificationType.USER_ACTION);
        n.setSeverity("info");
        n.setRead(true);
        n.setCreatedAt(LocalDateTime.now());
        when(appNotificationRepository.save(any(AppNotification.class))).thenReturn(n);

        AppNotificationResponse result = appNotificationService.saveUserAction(1L, "msg", null, "/link");

        assertThat(result.type()).isEqualTo(AppNotificationType.USER_ACTION);
    }

    @Test
    void saveUserAction_severidadeExplicitada_usaSeveridadeInformada() {
        AppNotification n = new AppNotification();
        n.setId(11L);
        n.setUserId(1L);
        n.setType(AppNotificationType.USER_ACTION);
        n.setSeverity("warning");
        n.setRead(true);
        n.setCreatedAt(LocalDateTime.now());
        when(appNotificationRepository.save(any(AppNotification.class))).thenReturn(n);

        AppNotificationResponse result = appNotificationService.saveUserAction(1L, "msg", "warning", null);

        assertThat(result.severity()).isEqualTo("warning");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static Goal savingsGoal(Long id, Long userId, String name, Double target) {
        Goal g = new Goal();
        g.setId(id);
        g.setUserId(userId);
        g.setName(name);
        g.setTargetAmount(target);
        g.setType(GoalType.SAVINGS);
        g.setStatus(GoalStatus.ACTIVE);
        g.setNotifyAt50(false);
        g.setNotifyAt75(false);
        g.setNotifyAt90(false);
        g.setNotifyOnComplete(false);
        g.setNotifyOnExceed(false);
        return g;
    }

    private static Goal expenseLimitGoal(Long id, Long userId, String name, Double target) {
        Goal g = savingsGoal(id, userId, name, target);
        g.setType(GoalType.EXPENSE_LIMIT);
        return g;
    }

    private static AppNotification notification(Long id, Long userId, AppNotificationType type) {
        AppNotification n = new AppNotification();
        n.setId(id);
        n.setUserId(userId);
        n.setType(type);
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.now());
        return n;
    }
}
