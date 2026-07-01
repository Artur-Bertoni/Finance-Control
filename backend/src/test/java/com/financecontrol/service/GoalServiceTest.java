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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings({"null", "unchecked"})
@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock GoalRepository            goalRepository;
    @Mock CategoryRepository        categoryRepository;
    @Mock TransactionLocaleRepository transactionLocaleRepository;
    @Mock TransactionRepository     transactionRepository;
    @Mock HistoryService            historyService;

    @InjectMocks GoalService goalService;

    @Test
    void create_metaEconomia_salvaSemErro() {
        Long userId = 1L;
        GoalRequest req = reqEconomia("Viagem", 5000.0);

        when(goalRepository.findPotentialDuplicates(any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> {
            Goal g = inv.getArgument(0);
            g.setId(1L);
            return g;
        });

        GoalResponse resp = goalService.create(userId, req, false);

        assertThat(resp.name()).isEqualTo("Viagem");
        assertThat(resp.targetAmount()).isEqualTo(5000.0);
        assertThat(resp.type()).isEqualTo(GoalType.SAVINGS);
        verify(historyService).recordCreation(HistoryService.ENTITY_GOAL, 1L, userId);
    }

    @Test
    void create_metaDuplicada_lancaBusinessException() {
        Long userId = 1L;
        GoalRequest req = reqEconomia("Viagem", 5000.0);

        Goal existente = goalWith(1L, userId, "Viagem", GoalType.SAVINGS, 5000.0, GoalStatus.ACTIVE);
        when(goalRepository.findPotentialDuplicates(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(existente));

        assertThatThrownBy(() -> goalService.create(userId, req, false))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void create_metaDuplicadaComForce_salvaSemErro() {
        Long userId = 1L;
        GoalRequest req = reqEconomia("Viagem", 5000.0);

        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> {
            Goal g = inv.getArgument(0);
            g.setId(2L);
            return g;
        });

        assertThatCode(() -> goalService.create(userId, req, true)).doesNotThrowAnyException();
        verify(goalRepository, never()).findPotentialDuplicates(any(), any(), any(), any(), any(), any());
    }

    @Test
    void findAllByUser_retornaMetasDoUsuario() {
        Long userId = 1L;
        Goal meta = goalWith(1L, userId, "Reserva", GoalType.SAVINGS, 10000.0, GoalStatus.ACTIVE);
        meta.setStartDate(LocalDate.now().minusDays(1));
        meta.setEndDate(LocalDate.now().plusMonths(6));

        when(goalRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(meta));
        when(transactionRepository.sumForGoal(any(), any(), any(), any())).thenReturn(0.0);

        List<GoalResponse> result = goalService.findAllByUser(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Reserva");
    }

    @Test
    void findById_naoEncontrada_lancaResourceNotFoundException() {
        when(goalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.findById(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void archive_metaAtiva_arquivaComSucesso() {
        Long userId = 1L;
        Goal meta = goalWith(1L, userId, "Reserva", GoalType.SAVINGS, 1000.0, GoalStatus.ACTIVE);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(meta));

        goalService.archive(1L, 1L);

        assertThat(meta.getStatus()).isEqualTo(GoalStatus.ARCHIVED);
        verify(goalRepository).save(meta);
    }

    @Test
    void update_happyPath_salvaERegistraDiff() {
        Long userId = 1L;
        Goal meta = goalWith(1L, userId, "Reserva", GoalType.SAVINGS, 1000.0, GoalStatus.ACTIVE);
        meta.setStartDate(LocalDate.now());
        meta.setEndDate(LocalDate.now().plusMonths(6));

        GoalRequest req = new GoalRequest("Reserva Nova", "desc", GoalType.SAVINGS, 2000.0,
                LocalDate.now(), LocalDate.now().plusMonths(12),
                Collections.emptyList(), Collections.emptyList(),
                null, null, null, null, null, null);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(meta));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.sumForGoal(any(), any(), any(), any())).thenReturn(0.0);

        GoalResponse resp = goalService.update(1L, userId, req);

        assertThat(resp.name()).isEqualTo("Reserva Nova");
        assertThat(resp.targetAmount()).isEqualTo(2000.0);
        verify(goalRepository).save(meta);
        verify(historyService).recordChanges(eq(HistoryService.ENTITY_GOAL), eq(1L), eq(userId), anyMap());
    }

    @Test
    void calculateCurrentAmount_savings_usaCredit() {
        Goal meta = goalWith(1L, 1L, "Reserva", GoalType.SAVINGS, 1000.0, GoalStatus.ACTIVE);
        meta.setStartDate(LocalDate.now().minusMonths(1));
        meta.setEndDate(LocalDate.now().plusMonths(1));

        when(transactionRepository.sumForGoal(eq(1L), any(), any(), eq(TransactionType.CREDIT)))
                .thenReturn(500.0);

        double result = goalService.calculateCurrentAmount(meta);

        assertThat(result).isEqualTo(500.0);
    }

    @Test
    void calculateCurrentAmount_income_usaCredit() {
        Goal meta = goalWith(1L, 1L, "Receita", GoalType.INCOME, 3000.0, GoalStatus.ACTIVE);
        meta.setStartDate(LocalDate.now().minusMonths(1));

        when(transactionRepository.sumForGoal(eq(1L), any(), any(), eq(TransactionType.CREDIT)))
                .thenReturn(1200.0);

        assertThat(goalService.calculateCurrentAmount(meta)).isEqualTo(1200.0);
    }

    @Test
    void calculateCurrentAmount_expenseLimit_usaDebit() {
        Goal meta = goalWith(1L, 1L, "Limite", GoalType.EXPENSE_LIMIT, 800.0, GoalStatus.ACTIVE);
        meta.setStartDate(LocalDate.now().minusMonths(1));

        when(transactionRepository.sumForGoal(eq(1L), any(), any(), eq(TransactionType.DEBIT)))
                .thenReturn(300.0);

        assertThat(goalService.calculateCurrentAmount(meta)).isEqualTo(300.0);
    }

    @Test
    void calculateCurrentAmount_resultadoNull_retornaZero() {
        Goal meta = goalWith(1L, 1L, "Reserva", GoalType.SAVINGS, 1000.0, GoalStatus.ACTIVE);
        meta.setStartDate(LocalDate.now().minusMonths(1));

        when(transactionRepository.sumForGoal(any(), any(), any(), any())).thenReturn(null);

        assertThat(goalService.calculateCurrentAmount(meta)).isEqualTo(0.0);
    }

    @Test
    void findById_metaEconomiaCompleta_marcaComoCompleted() {
        Goal meta = goalWith(1L, 1L, "Reserva", GoalType.SAVINGS, 1000.0, GoalStatus.ACTIVE);
        meta.setStartDate(LocalDate.now().minusMonths(1));
        meta.setEndDate(LocalDate.now().plusMonths(1));

        when(goalRepository.findById(1L)).thenReturn(Optional.of(meta));
        when(transactionRepository.sumForGoal(any(), any(), any(), any())).thenReturn(1500.0);
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));

        GoalResponse resp = goalService.findById(1L, 1L);

        assertThat(meta.getStatus()).isEqualTo(GoalStatus.COMPLETED);
        assertThat(resp.status()).isEqualTo(GoalStatus.COMPLETED);
        verify(goalRepository).save(meta);
    }

    @Test
    void delete_metaExistente_chamaRepository() {
        Goal meta = goalWith(7L, 1L, "Reserva", GoalType.SAVINGS, 1000.0, GoalStatus.ACTIVE);
        when(goalRepository.findById(7L)).thenReturn(Optional.of(meta));

        goalService.delete(7L, 1L);

        verify(goalRepository).deleteById(7L);
    }

    @Test
    void delete_metaNaoEncontrada_lancaResourceNotFoundException() {
        when(goalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.delete(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAllByUser_tiposDiferentes_retornaTodos() {
        Long userId = 1L;
        Goal savings = goalWith(1L, userId, "Reserva", GoalType.SAVINGS, 1000.0, GoalStatus.ACTIVE);
        savings.setStartDate(LocalDate.now().minusMonths(1));
        Goal limit = goalWith(2L, userId, "Limite", GoalType.EXPENSE_LIMIT, 800.0, GoalStatus.ACTIVE);
        limit.setStartDate(LocalDate.now().minusMonths(1));

        when(goalRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(savings, limit));
        when(transactionRepository.sumForGoal(any(), any(), any(), any())).thenReturn(100.0);

        List<GoalResponse> result = goalService.findAllByUser(userId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(GoalResponse::type)
                .containsExactly(GoalType.SAVINGS, GoalType.EXPENSE_LIMIT);
    }

    // ── new coverage tests ───────────────────────────────────────────────────

    @Test
    void create_comCategoriasELocais_carregaEntidades() {
        Long userId = 1L;
        Category cat = categoryWith(11L, "Mercado");
        TransactionLocale loc = localeWith(21L, "Centro");

        GoalRequest req = new GoalRequest("Reserva", "desc", GoalType.SAVINGS, 5000.0,
                LocalDate.now(), LocalDate.now().plusMonths(12),
                List.of(11L), List.of(21L),
                null, null, null, null, null, null);

        when(goalRepository.findPotentialDuplicates(any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(categoryRepository.findById(11L)).thenReturn(Optional.of(cat));
        when(transactionLocaleRepository.findById(21L)).thenReturn(Optional.of(loc));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> {
            Goal g = inv.getArgument(0);
            g.setId(1L);
            return g;
        });

        GoalResponse resp = goalService.create(userId, req, false);

        assertThat(resp.name()).isEqualTo("Reserva");
        verify(categoryRepository).findById(11L);
        verify(transactionLocaleRepository).findById(21L);
    }

    @Test
    void create_categoriaNaoEncontrada_lancaResourceNotFoundException() {
        GoalRequest req = new GoalRequest("Reserva", null, GoalType.SAVINGS, 5000.0,
                LocalDate.now(), LocalDate.now().plusMonths(12),
                List.of(99L), Collections.emptyList(),
                null, null, null, null, null, null);

        when(goalRepository.findPotentialDuplicates(any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.create(1L, req, false))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_localeNaoEncontrado_lancaResourceNotFoundException() {
        GoalRequest req = new GoalRequest("Reserva", null, GoalType.SAVINGS, 5000.0,
                LocalDate.now(), LocalDate.now().plusMonths(12),
                Collections.emptyList(), List.of(99L),
                null, null, null, null, null, null);

        when(goalRepository.findPotentialDuplicates(any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(transactionLocaleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.create(1L, req, false))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findById_limiteDespesaAtingido_naoMarcaComoCompleted() {
        Goal meta = goalWith(1L, 1L, "Limite", GoalType.EXPENSE_LIMIT, 800.0, GoalStatus.ACTIVE);
        meta.setStartDate(LocalDate.now().minusMonths(1));
        meta.setEndDate(LocalDate.now().plusMonths(1));

        when(goalRepository.findById(1L)).thenReturn(Optional.of(meta));
        when(transactionRepository.sumForGoal(any(), any(), any(), any())).thenReturn(1000.0);

        GoalResponse resp = goalService.findById(1L, 1L);

        assertThat(meta.getStatus()).isEqualTo(GoalStatus.ACTIVE);
        assertThat(resp.status()).isEqualTo(GoalStatus.ACTIVE);
        verify(goalRepository, never()).save(any());
    }

    @Test
    void findById_metaSavingsAbaixoDoAlvo_permaneceAtiva() {
        Goal meta = goalWith(1L, 1L, "Reserva", GoalType.SAVINGS, 1000.0, GoalStatus.ACTIVE);
        meta.setStartDate(LocalDate.now().minusMonths(1));
        meta.setEndDate(LocalDate.now().plusMonths(1));

        when(goalRepository.findById(1L)).thenReturn(Optional.of(meta));
        when(transactionRepository.sumForGoal(any(), any(), any(), any())).thenReturn(300.0);

        GoalResponse resp = goalService.findById(1L, 1L);

        assertThat(resp.status()).isEqualTo(GoalStatus.ACTIVE);
        verify(goalRepository, never()).save(any());
    }

    @Test
    void update_mudaTipoEDatas_registraDiffDessesCampos() {
        Long userId = 1L;
        Goal meta = goalWith(1L, userId, "Reserva", GoalType.SAVINGS, 1000.0, GoalStatus.ACTIVE);
        meta.setDescription("antiga");
        meta.setStartDate(LocalDate.of(2025, 1, 1));
        meta.setEndDate(LocalDate.of(2025, 6, 1));

        GoalRequest req = new GoalRequest("Limite Novo", "nova", GoalType.EXPENSE_LIMIT, 1500.0,
                LocalDate.of(2025, 2, 1), LocalDate.of(2025, 12, 1),
                Collections.emptyList(), Collections.emptyList(),
                false, false, false, false, false, false);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(meta));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.sumForGoal(any(), any(), any(), any())).thenReturn(0.0);

        GoalResponse resp = goalService.update(1L, userId, req);

        assertThat(resp.type()).isEqualTo(GoalType.EXPENSE_LIMIT);

        ArgumentCaptor<Map<String, String[]>> diffCaptor = ArgumentCaptor.forClass(Map.class);
        verify(historyService).recordChanges(eq(HistoryService.ENTITY_GOAL), eq(1L), eq(userId), diffCaptor.capture());

        Map<String, String[]> diff = diffCaptor.getValue();
        assertThat(diff).containsKeys("name", "description", "type", "targetAmount",
                "startDate", "endDate", "notifyAt50", "notifyOnExceed");
    }

    @Test
    void update_mudaCategoriasELocais_registraDiffComNomes() {
        Long userId = 1L;
        Goal meta = goalWith(1L, userId, "Reserva", GoalType.SAVINGS, 1000.0, GoalStatus.ACTIVE);
        meta.setStartDate(LocalDate.now());
        meta.setEndDate(LocalDate.now().plusMonths(6));

        Category cat = categoryWith(11L, "Mercado");
        TransactionLocale loc = localeWith(21L, "Centro");

        GoalRequest req = new GoalRequest("Reserva", null, GoalType.SAVINGS, 1000.0,
                LocalDate.now(), LocalDate.now().plusMonths(6),
                List.of(11L), List.of(21L),
                null, null, null, null, null, null);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(meta));
        when(categoryRepository.findAllById(List.of(11L))).thenReturn(List.of(cat));
        when(transactionLocaleRepository.findAllById(List.of(21L))).thenReturn(List.of(loc));
        when(categoryRepository.findById(11L)).thenReturn(Optional.of(cat));
        when(transactionLocaleRepository.findById(21L)).thenReturn(Optional.of(loc));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.sumForGoalByCategoriesAndLocales(any(), any(), any(), any(), any(), any()))
                .thenReturn(0.0);

        goalService.update(1L, userId, req);

        ArgumentCaptor<Map<String, String[]>> diffCaptor = ArgumentCaptor.forClass(Map.class);
        verify(historyService).recordChanges(eq(HistoryService.ENTITY_GOAL), eq(1L), eq(userId), diffCaptor.capture());

        Map<String, String[]> diff = diffCaptor.getValue();
        assertThat(diff).containsKeys("categories", "locales");
        assertThat(diff.get("categories")[1]).isEqualTo("Mercado");
        assertThat(diff.get("locales")[1]).isEqualTo("Centro");
    }

    @Test
    void archive_metaJaArquivada_registraDiffMesmoValor() {
        Goal meta = goalWith(1L, 1L, "Reserva", GoalType.SAVINGS, 1000.0, GoalStatus.ARCHIVED);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(meta));

        goalService.archive(1L, 1L);

        assertThat(meta.getStatus()).isEqualTo(GoalStatus.ARCHIVED);
        verify(goalRepository).save(meta);
        verify(historyService).recordChanges(eq(HistoryService.ENTITY_GOAL), eq(1L), eq(1L), anyMap());
    }

    @Test
    void archive_metaNaoEncontrada_lancaResourceNotFoundException() {
        when(goalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.archive(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void calculateCurrentAmount_apenasCategorias_usaQueryPorCategorias() {
        Goal meta = goalWith(1L, 1L, "Reserva", GoalType.SAVINGS, 1000.0, GoalStatus.ACTIVE);
        meta.setStartDate(LocalDate.now().minusMonths(1));
        meta.getCategories().add(categoryWith(11L, "Mercado"));

        when(transactionRepository.sumForGoalByCategories(eq(1L), any(), any(), eq(TransactionType.CREDIT), eq(List.of(11L))))
                .thenReturn(250.0);

        assertThat(goalService.calculateCurrentAmount(meta)).isEqualTo(250.0);
    }

    @Test
    void calculateCurrentAmount_apenasLocais_usaQueryPorLocais() {
        Goal meta = goalWith(1L, 1L, "Reserva", GoalType.SAVINGS, 1000.0, GoalStatus.ACTIVE);
        meta.setStartDate(LocalDate.now().minusMonths(1));
        meta.getLocales().add(localeWith(21L, "Centro"));

        when(transactionRepository.sumForGoalByLocales(eq(1L), any(), any(), eq(TransactionType.CREDIT), eq(List.of(21L))))
                .thenReturn(125.0);

        assertThat(goalService.calculateCurrentAmount(meta)).isEqualTo(125.0);
    }

    @Test
    void calculateCurrentAmount_endDatePassada_usaEndDate() {
        Goal meta = goalWith(1L, 1L, "Reserva", GoalType.SAVINGS, 1000.0, GoalStatus.ACTIVE);
        meta.setStartDate(LocalDate.now().minusMonths(3));
        LocalDate past = LocalDate.now().minusMonths(1);
        meta.setEndDate(past);

        when(transactionRepository.sumForGoal(eq(1L), any(), eq(past), eq(TransactionType.CREDIT)))
                .thenReturn(700.0);

        assertThat(goalService.calculateCurrentAmount(meta)).isEqualTo(700.0);
        verify(transactionRepository).sumForGoal(eq(1L), any(), eq(past), eq(TransactionType.CREDIT));
    }

    // ── update per-field buildDiff branch coverage ──────────────────────────

    private Goal baselineGoal() {
        Goal g = goalWith(1L, 1L, "Reserva", GoalType.SAVINGS, 1000.0, GoalStatus.ACTIVE);
        g.setDescription("desc");
        g.setStartDate(LocalDate.of(2025, 1, 1));
        g.setEndDate(LocalDate.of(2025, 12, 1));
        g.setNotifyAt50(true);
        g.setNotifyAt75(true);
        g.setNotifyAt90(true);
        g.setNotifyOnComplete(true);
        g.setNotifyOnDeadline(true);
        g.setNotifyOnExceed(true);
        return g;
    }

    private static GoalRequest baselineReq(String name, String description, Double target,
                                           LocalDate start, LocalDate end,
                                           Boolean n50, Boolean n75, Boolean n90,
                                           Boolean onComplete, Boolean onDeadline, Boolean onExceed) {
        return new GoalRequest(name, description, GoalType.SAVINGS, target, start, end,
                Collections.emptyList(), Collections.emptyList(),
                n50, n75, n90, onComplete, onDeadline, onExceed);
    }

    private Map<String, String[]> captureUpdateDiff(Goal existing, GoalRequest req) {
        when(goalRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.sumForGoal(any(), any(), any(), any())).thenReturn(0.0);

        goalService.update(1L, 1L, req);

        ArgumentCaptor<Map<String, String[]>> diff = ArgumentCaptor.forClass(Map.class);
        verify(historyService).recordChanges(eq(HistoryService.ENTITY_GOAL), eq(1L), eq(1L), diff.capture());
        return diff.getValue();
    }

    @Test
    void update_mudaApenasDescricao_registraDiffDescription() {
        Map<String, String[]> diff = captureUpdateDiff(baselineGoal(),
                baselineReq("Reserva", "nova desc", 1000.0, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 1),
                        true, true, true, true, true, true));
        assertThat(diff).containsOnlyKeys("description");
    }

    @Test
    void update_mudaApenasTargetAmount_registraDiffTargetAmount() {
        Map<String, String[]> diff = captureUpdateDiff(baselineGoal(),
                baselineReq("Reserva", "desc", 9999.0, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 1),
                        true, true, true, true, true, true));
        assertThat(diff).containsOnlyKeys("targetAmount");
    }

    @Test
    void update_mudaApenasStartDate_registraDiffStartDate() {
        Map<String, String[]> diff = captureUpdateDiff(baselineGoal(),
                baselineReq("Reserva", "desc", 1000.0, LocalDate.of(2025, 2, 1), LocalDate.of(2025, 12, 1),
                        true, true, true, true, true, true));
        assertThat(diff).containsOnlyKeys("startDate");
    }

    @Test
    void update_mudaApenasEndDate_registraDiffEndDate() {
        Map<String, String[]> diff = captureUpdateDiff(baselineGoal(),
                baselineReq("Reserva", "desc", 1000.0, LocalDate.of(2025, 1, 1), LocalDate.of(2026, 1, 1),
                        true, true, true, true, true, true));
        assertThat(diff).containsOnlyKeys("endDate");
    }

    @Test
    void update_mudaApenasNotifyAt50_registraDiffNotifyAt50() {
        Map<String, String[]> diff = captureUpdateDiff(baselineGoal(),
                baselineReq("Reserva", "desc", 1000.0, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 1),
                        false, true, true, true, true, true));
        assertThat(diff).containsOnlyKeys("notifyAt50");
    }

    @Test
    void update_mudaApenasNotifyAt75_registraDiffNotifyAt75() {
        Map<String, String[]> diff = captureUpdateDiff(baselineGoal(),
                baselineReq("Reserva", "desc", 1000.0, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 1),
                        true, false, true, true, true, true));
        assertThat(diff).containsOnlyKeys("notifyAt75");
    }

    @Test
    void update_mudaApenasNotifyAt90_registraDiffNotifyAt90() {
        Map<String, String[]> diff = captureUpdateDiff(baselineGoal(),
                baselineReq("Reserva", "desc", 1000.0, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 1),
                        true, true, false, true, true, true));
        assertThat(diff).containsOnlyKeys("notifyAt90");
    }

    @Test
    void update_mudaApenasNotifyOnComplete_registraDiffNotifyOnComplete() {
        Map<String, String[]> diff = captureUpdateDiff(baselineGoal(),
                baselineReq("Reserva", "desc", 1000.0, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 1),
                        true, true, true, false, true, true));
        assertThat(diff).containsOnlyKeys("notifyOnComplete");
    }

    @Test
    void update_mudaApenasNotifyOnDeadline_registraDiffNotifyOnDeadline() {
        Map<String, String[]> diff = captureUpdateDiff(baselineGoal(),
                baselineReq("Reserva", "desc", 1000.0, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 1),
                        true, true, true, true, false, true));
        assertThat(diff).containsOnlyKeys("notifyOnDeadline");
    }

    @Test
    void update_mudaApenasNotifyOnExceed_registraDiffNotifyOnExceed() {
        Map<String, String[]> diff = captureUpdateDiff(baselineGoal(),
                baselineReq("Reserva", "desc", 1000.0, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 1),
                        true, true, true, true, true, false));
        assertThat(diff).containsOnlyKeys("notifyOnExceed");
    }

    private static Category categoryWith(Long id, String name) {
        Category c = new Category();
        c.setId(id);
        c.setUserId(1L);
        c.setName(name);
        return c;
    }

    private static TransactionLocale localeWith(Long id, String name) {
        TransactionLocale l = new TransactionLocale();
        l.setId(id);
        l.setUserId(1L);
        l.setName(name);
        return l;
    }

    private static GoalRequest reqEconomia(String name, double target) {
        return new GoalRequest(name, null, GoalType.SAVINGS, target,
                LocalDate.now(), LocalDate.now().plusMonths(12),
                Collections.emptyList(), Collections.emptyList(),
                null, null, null, null, null, null);
    }

    private static Goal goalWith(Long id, Long userId, String name,
                                  GoalType type, double target, GoalStatus status) {
        Goal g = new Goal();
        g.setId(id);
        g.setUserId(userId);
        g.setName(name);
        g.setType(type);
        g.setTargetAmount(target);
        g.setStatus(status);
        g.setCreatedAt(LocalDateTime.now());
        return g;
    }
}
