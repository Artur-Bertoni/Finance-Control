package com.financecontrol.service.finny;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financecontrol.dto.response.FinnyStatsResponse;
import com.financecontrol.dto.response.FinnyTipResponse;
import com.financecontrol.entity.FinnyTip;
import com.financecontrol.entity.FinnyTipPreference;
import com.financecontrol.enums.FinnyTipCategory;
import com.financecontrol.enums.FinnyTipFeedback;
import com.financecontrol.enums.FinnyTipStatus;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.FinnyTipPreferenceRepository;
import com.financecontrol.repository.FinnyTipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class FinnyAgentServiceTest {

    @Mock FinancialProfileService profileService;
    @Mock FinnyTipRepository tipRepository;
    @Mock FinnyTipPreferenceRepository preferenceRepository;

    private FinnyAgentService service;
    private final List<FinnyTip> saved = new ArrayList<>();

    private static final FinancialProfile DUMMY = new FinancialProfile(
            true, 6, 1000, 600, 400, 40, 5000, 600, 8.3, null, List.of(), List.of());

    private record FakeRule(List<TipCandidate> out) implements TipRule {
        @Override public List<TipCandidate> evaluate(FinancialProfile p) { return out; }
    }

    private void buildWith(TipCandidate... candidates) {
        service = new FinnyAgentService(
                List.of(new FakeRule(List.of(candidates))),
                profileService, tipRepository, preferenceRepository, new ObjectMapper());
    }

    /** save() atribui id incremental e guarda o tip para o teste inspecionar / devolver em queries. */
    private void stubSave() {
        when(tipRepository.save(any())).thenAnswer(inv -> {
            FinnyTip t = inv.getArgument(0);
            if (t.getId() == null) t.setId((long) (saved.size() + 1));
            if (!saved.contains(t)) saved.add(t);
            return t;
        });
    }

    @BeforeEach
    void stubProfile() {
        lenient().when(profileService.build(1L)).thenReturn(DUMMY);
    }

    @Test
    void generateTips_persisteComoNewEDevolveAtivos() {
        buildWith(new TipCandidate("OVERSPENDING", FinnyTipCategory.BUDGET, "warning", 95, Map.of("pct", 20L)));
        when(tipRepository.existsByUserIdAndRuleKeyAndFeedbackAtAfter(any(), any(), any())).thenReturn(false);
        when(tipRepository.findFirstByUserIdAndRuleKeyAndStatusInOrderByCreatedAtDesc(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(preferenceRepository.findByUserId(1L)).thenReturn(List.of());
        stubSave();
        when(tipRepository.findByUserIdAndStatusInOrderByScoreDesc(eq(1L), any())).thenAnswer(inv -> saved);

        List<FinnyTipResponse> result = service.generateTips(1L, "pt");

        assertThat(saved).singleElement().satisfies(t -> assertThat(t.getStatus()).isEqualTo(FinnyTipStatus.NEW));
        assertThat(result).singleElement().satisfies(r -> {
            assertThat(r.ruleKey()).isEqualTo("OVERSPENDING");
            assertThat(r.params()).containsEntry("pct", 20);
        });
    }

    @Test
    void generateTips_pesoAdaptativoAfetaScorePersistido() {
        buildWith(
                new TipCandidate("A", FinnyTipCategory.BUDGET,  "info", 50, Map.of()),
                new TipCandidate("B", FinnyTipCategory.SAVINGS, "info", 40, Map.of()));
        when(tipRepository.existsByUserIdAndRuleKeyAndFeedbackAtAfter(any(), any(), any())).thenReturn(false);
        when(tipRepository.findFirstByUserIdAndRuleKeyAndStatusInOrderByCreatedAtDesc(any(), any(), any()))
                .thenReturn(Optional.empty());
        FinnyTipPreference savingsPref = new FinnyTipPreference();
        savingsPref.setCategory(FinnyTipCategory.SAVINGS);
        savingsPref.setWeight(2.0); // SAVINGS 40*2=80 > BUDGET 50*1=50
        when(preferenceRepository.findByUserId(1L)).thenReturn(List.of(savingsPref));
        stubSave();
        when(tipRepository.findByUserIdAndStatusInOrderByScoreDesc(eq(1L), any())).thenAnswer(inv -> saved);

        service.generateTips(1L, "pt");

        double scoreB = saved.stream().filter(t -> t.getRuleKey().equals("B")).findFirst().orElseThrow().getScore();
        double scoreA = saved.stream().filter(t -> t.getRuleKey().equals("A")).findFirst().orElseThrow().getScore();
        assertThat(scoreB).isGreaterThan(scoreA);
    }

    @Test
    void generateTips_regraJaAtiva_naoDuplica() {
        buildWith(new TipCandidate("TOP_CATEGORY", FinnyTipCategory.BUDGET, "info", 65, Map.of()));
        when(tipRepository.existsByUserIdAndRuleKeyAndFeedbackAtAfter(any(), any(), any())).thenReturn(false);
        when(preferenceRepository.findByUserId(1L)).thenReturn(List.of());
        FinnyTip existing = new FinnyTip();
        existing.setId(7L);
        existing.setRuleKey("TOP_CATEGORY");
        existing.setStatus(FinnyTipStatus.SHOWN);
        when(tipRepository.findFirstByUserIdAndRuleKeyAndStatusInOrderByCreatedAtDesc(any(), any(), any()))
                .thenReturn(Optional.of(existing));
        when(tipRepository.findByUserIdAndStatusInOrderByScoreDesc(eq(1L), any())).thenReturn(List.of(existing));

        service.generateTips(1L, "pt");

        verify(tipRepository, never()).save(any());
    }

    @Test
    void generateTips_regraDispensadaRecentemente_ehSuprimida() {
        buildWith(new TipCandidate("BALANCE_DROP", FinnyTipCategory.CASHFLOW, "warning", 70, Map.of()));
        when(tipRepository.existsByUserIdAndRuleKeyAndFeedbackAtAfter(
                eq(1L), eq("BALANCE_DROP"), any())).thenReturn(true);
        when(preferenceRepository.findByUserId(1L)).thenReturn(List.of());
        when(tipRepository.findByUserIdAndStatusInOrderByScoreDesc(eq(1L), any())).thenReturn(List.of());

        assertThat(service.generateTips(1L, "pt")).isEmpty();
        verify(tipRepository, never()).save(any());
    }

    @Test
    void markShown_moveNewParaShown() {
        buildWith();
        FinnyTip tip = new FinnyTip();
        tip.setId(3L);
        tip.setUserId(1L);
        tip.setStatus(FinnyTipStatus.NEW);
        when(tipRepository.findById(3L)).thenReturn(Optional.of(tip));
        when(tipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.markShown(1L, 3L);

        assertThat(tip.getStatus()).isEqualTo(FinnyTipStatus.SHOWN);
        assertThat(tip.getShownAt()).isNotNull();
    }

    @Test
    void recordFeedback_helpful_moveStatusEPeso() {
        buildWith();
        FinnyTip tip = new FinnyTip();
        tip.setId(5L);
        tip.setUserId(1L);
        tip.setCategory(FinnyTipCategory.SAVINGS);
        tip.setStatus(FinnyTipStatus.SHOWN);
        when(tipRepository.findById(5L)).thenReturn(Optional.of(tip));
        FinnyTipPreference pref = new FinnyTipPreference();
        pref.setCategory(FinnyTipCategory.SAVINGS);
        pref.setWeight(1.0);
        when(preferenceRepository.findByUserIdAndCategory(1L, FinnyTipCategory.SAVINGS)).thenReturn(Optional.of(pref));

        service.recordFeedback(1L, 5L, FinnyTipFeedback.HELPFUL);

        assertThat(tip.getStatus()).isEqualTo(FinnyTipStatus.HELPFUL);
        assertThat(tip.getFeedbackAt()).isNotNull();
        assertThat(pref.getWeight()).isEqualTo(1.25);
        assertThat(pref.getHelpfulCount()).isEqualTo(1);
    }

    @Test
    void recordFeedback_trocaDeHelpfulParaDismissed_reverteAntesDeAplicar() {
        buildWith();
        FinnyTip tip = new FinnyTip();
        tip.setId(5L);
        tip.setUserId(1L);
        tip.setCategory(FinnyTipCategory.SAVINGS);
        tip.setStatus(FinnyTipStatus.HELPFUL); // já tinha feedback
        when(tipRepository.findById(5L)).thenReturn(Optional.of(tip));
        FinnyTipPreference pref = new FinnyTipPreference();
        pref.setCategory(FinnyTipCategory.SAVINGS);
        pref.setWeight(1.25);
        pref.setHelpfulCount(1);
        when(preferenceRepository.findByUserIdAndCategory(1L, FinnyTipCategory.SAVINGS)).thenReturn(Optional.of(pref));

        service.recordFeedback(1L, 5L, FinnyTipFeedback.DISMISSED);

        assertThat(tip.getStatus()).isEqualTo(FinnyTipStatus.DISMISSED);
        // reverte HELPFUL (1.25 -> 1.00, count 0) e aplica DISMISSED (1.00 -> 0.90, count 1)
        assertThat(pref.getWeight()).isEqualTo(0.90);
        assertThat(pref.getHelpfulCount()).isZero();
        assertThat(pref.getDismissedCount()).isEqualTo(1);
    }

    @Test
    void recordFeedback_dicaDeOutroUsuario_lancaNotFound() {
        buildWith();
        FinnyTip tip = new FinnyTip();
        tip.setId(5L);
        tip.setUserId(2L);
        when(tipRepository.findById(5L)).thenReturn(Optional.of(tip));

        assertThatThrownBy(() -> service.recordFeedback(1L, 5L, FinnyTipFeedback.DISMISSED))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void nudgeWeight_respeitaTetoMaximo() {
        buildWith();
        FinnyTipPreference pref = new FinnyTipPreference();
        pref.setCategory(FinnyTipCategory.BUDGET);
        pref.setWeight(2.95);
        when(preferenceRepository.findByUserIdAndCategory(1L, FinnyTipCategory.BUDGET)).thenReturn(Optional.of(pref));

        service.nudgeWeight(1L, FinnyTipCategory.BUDGET, 0.5);

        assertThat(pref.getWeight()).isEqualTo(3.0);
    }

    @Test
    void getStats_agregaContagensEPerfilAtual() {
        buildWith();
        when(tipRepository.countByUserId(1L)).thenReturn(10L);
        when(tipRepository.countByUserIdAndStatus(1L, FinnyTipStatus.HELPFUL)).thenReturn(4L);
        when(tipRepository.countByUserIdAndStatus(1L, FinnyTipStatus.NOT_HELPFUL)).thenReturn(1L);
        when(tipRepository.countByUserIdAndStatus(1L, FinnyTipStatus.DISMISSED)).thenReturn(2L);
        List<Object[]> catRows = new ArrayList<>();
        catRows.add(new Object[]{FinnyTipCategory.BUDGET, 6L});
        when(tipRepository.countByCategory(1L)).thenReturn(catRows);

        FinnyStatsResponse stats = service.getStats(1L);

        assertThat(stats.totalTips()).isEqualTo(10L);
        assertThat(stats.helpfulCount()).isEqualTo(4L);
        assertThat(stats.byCategory()).containsEntry("BUDGET", 6L);
        assertThat(stats.savingsRatePct()).isEqualTo(40.0);
        assertThat(stats.currentBalance()).isEqualTo(5000.0);
    }
}
