package com.financecontrol.service;

import com.financecontrol.dto.response.ChangeGroupResponse;
import com.financecontrol.entity.EntityChangeLog;
import com.financecontrol.repository.EntityChangeLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @Mock EntityChangeLogRepository repository;

    @InjectMocks HistoryService historyService;

    // ── static helper tests ──────────────────────────────────────────────────

    @Test
    void differs_valoresIguais_retornaFalse() {
        assertThat(HistoryService.differs("abc", "abc")).isFalse();
    }

    @Test
    void differs_valoresDiferentes_retornaTrue() {
        assertThat(HistoryService.differs("abc", "xyz")).isTrue();
    }

    @Test
    void differs_ambosNulos_retornaFalse() {
        assertThat(HistoryService.differs(null, null)).isFalse();
    }

    @Test
    void differs_umNuloOutroNaoNulo_retornaTrue() {
        assertThat(HistoryService.differs(null, "x")).isTrue();
    }

    @Test
    void diff_string_retornaArrayComDoisElementos() {
        String[] result = HistoryService.diff("old", "new");
        assertThat(result).containsExactly("old", "new");
    }

    @Test
    void diff_stringBrancoViraNull() {
        String[] result = HistoryService.diff("   ", "new");
        assertThat(result[0]).isNull();
        assertThat(result[1]).isEqualTo("new");
    }

    @Test
    void diff_stringNulaViraNull() {
        String[] result = HistoryService.diff((String) null, "new");
        assertThat(result[0]).isNull();
    }

    @Test
    void diff_object_converteParaString() {
        String[] result = HistoryService.diff(42, 99);
        assertThat(result).containsExactly("42", "99");
    }

    @Test
    void diff_objectNulo_viraNull() {
        String[] result = HistoryService.diff((Object) null, 5);
        assertThat(result[0]).isNull();
        assertThat(result[1]).isEqualTo("5");
    }

    // ── recordCreation ───────────────────────────────────────────────────────

    @Test
    void recordCreation_salvaNoBanco() {
        when(repository.save(any(EntityChangeLog.class))).thenAnswer(inv -> inv.getArgument(0));

        historyService.recordCreation("account", 1L, 2L);

        ArgumentCaptor<EntityChangeLog> cap = ArgumentCaptor.forClass(EntityChangeLog.class);
        verify(repository).save(cap.capture());
        assertThat(cap.getValue().getFieldName()).isEqualTo("CREATED");
        assertThat(cap.getValue().getEntityType()).isEqualTo("account");
        assertThat(cap.getValue().getEntityId()).isEqualTo(1L);
    }

    // ── recordChanges ────────────────────────────────────────────────────────

    @Test
    void recordChanges_comDiff_salvaUmaEntradaPorCampo() {
        when(repository.save(any(EntityChangeLog.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, String[]> diff = Map.of(
                "name", new String[]{"old", "new"},
                "email", new String[]{"a@b.com", "c@d.com"}
        );
        historyService.recordChanges("user", 5L, 5L, diff);

        verify(repository, times(2)).save(any(EntityChangeLog.class));
    }

    @Test
    void recordChanges_diffVazio_naoSalva() {
        historyService.recordChanges("user", 1L, 1L, Map.of());
        verify(repository, never()).save(any());
    }

    @Test
    void recordChanges_diffNulo_naoSalva() {
        historyService.recordChanges("user", 1L, 1L, null);
        verify(repository, never()).save(any());
    }

    // ── recordPasswordChange ─────────────────────────────────────────────────

    @Test
    void recordPasswordChange_salvaNoBanco() {
        when(repository.save(any(EntityChangeLog.class))).thenAnswer(inv -> inv.getArgument(0));

        historyService.recordPasswordChange(10L);

        ArgumentCaptor<EntityChangeLog> cap = ArgumentCaptor.forClass(EntityChangeLog.class);
        verify(repository).save(cap.capture());
        assertThat(cap.getValue().getFieldName()).isEqualTo("PASSWORD_CHANGED");
        assertThat(cap.getValue().getEntityType()).isEqualTo("user");
    }

    // ── getHistory ───────────────────────────────────────────────────────────

    @Test
    void getHistory_logDeCriacao_retornaIsCreationTrue() {
        EntityChangeLog log = EntityChangeLog.of("account", 1L, 1L, "CREATED", null, null,
                LocalDateTime.now(), "grp-1");
        when(repository.findByEntityTypeAndEntityIdOrderByChangedAtDesc("account", 1L))
                .thenReturn(List.of(log));

        List<ChangeGroupResponse> result = historyService.getHistory("account", 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).creation()).isTrue();
        assertThat(result.get(0).changes()).isEmpty();
    }

    @Test
    void getHistory_logDeAlteracao_retornaChanges() {
        String groupId = "grp-2";
        EntityChangeLog log = EntityChangeLog.of("account", 2L, 2L, "name", "old", "new",
                LocalDateTime.now(), groupId);
        when(repository.findByEntityTypeAndEntityIdOrderByChangedAtDesc("account", 2L))
                .thenReturn(List.of(log));

        List<ChangeGroupResponse> result = historyService.getHistory("account", 2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).creation()).isFalse();
        assertThat(result.get(0).changes()).hasSize(1);
        assertThat(result.get(0).changes().get(0).fieldName()).isEqualTo("name");
    }

    @Test
    void getHistory_logPasswordChange_retornaIsPasswordChangeTrue() {
        EntityChangeLog log = EntityChangeLog.of("user", 3L, 3L, "PASSWORD_CHANGED", null, null,
                LocalDateTime.now(), "grp-3");
        when(repository.findByEntityTypeAndEntityIdOrderByChangedAtDesc("user", 3L))
                .thenReturn(List.of(log));

        List<ChangeGroupResponse> result = historyService.getHistory("user", 3L);

        assertThat(result.get(0).passwordChange()).isTrue();
        assertThat(result.get(0).changes()).isEmpty();
    }

    @Test
    void getHistory_semLogs_retornaListaVazia() {
        when(repository.findByEntityTypeAndEntityIdOrderByChangedAtDesc("account", 99L))
                .thenReturn(List.of());

        assertThat(historyService.getHistory("account", 99L)).isEmpty();
    }
}
