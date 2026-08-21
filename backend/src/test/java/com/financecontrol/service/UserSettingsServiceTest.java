package com.financecontrol.service;

import com.financecontrol.dto.request.UserSettingsRequest;
import com.financecontrol.dto.response.UserSettingsResponse;
import com.financecontrol.entity.UserSettings;
import com.financecontrol.repository.UserSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSettingsServiceTest {

    @Mock UserSettingsRepository repository;
    @Mock HistoryService historyService;

    @InjectMocks UserSettingsService service;

    private static UserSettings existing() {
        UserSettings s = new UserSettings();
        s.setId(7L);
        s.setUserId(1L);
        return s;
    }

    // ── getOrCreate ──────────────────────────────────────────────────────────

    @Test
    void getOrCreate_semRegistro_criaComTudoHabilitado() {
        when(repository.findByUserId(1L)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserSettings created = service.getOrCreate(1L);

        assertThat(created.getUserId()).isEqualTo(1L);
        assertThat(created.isReportsEnabled()).isTrue();
        assertThat(created.isBudgetsEnabled()).isTrue();
        assertThat(created.isGoalsEnabled()).isTrue();
        assertThat(created.isFinnyEnabled()).isTrue();
        assertThat(created.isStatementImportEnabled()).isTrue();
        assertThat(created.isInstitutionsEnabled()).isTrue();
        assertThat(created.isLocalesEnabled()).isTrue();
        assertThat(created.isEmailsEnabled()).isTrue();
        assertThat(created.getUpdatedAt()).isNotNull();
    }

    @Test
    void getOrCreate_comRegistro_naoSalvaDeNovo() {
        when(repository.findByUserId(1L)).thenReturn(Optional.of(existing()));

        service.getOrCreate(1L);

        verify(repository, never()).save(any());
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_desligaFuncionalidades_persisteERegistraHistorico() {
        UserSettings settings = existing();
        when(repository.findByUserId(1L)).thenReturn(Optional.of(settings));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserSettingsResponse result = service.update(1L,
                new UserSettingsRequest(false, null, false, null, null, null, null, null));

        assertThat(result.reportsEnabled()).isFalse();
        assertThat(result.goalsEnabled()).isFalse();
        assertThat(result.budgetsEnabled()).isTrue();
        assertThat(settings.isReportsEnabled()).isFalse();
        assertThat(settings.isGoalsEnabled()).isFalse();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String[]>> diff = ArgumentCaptor.forClass(Map.class);
        verify(historyService).recordChanges(eq("user"), eq(1L), eq(1L), diff.capture());
        assertThat(diff.getValue()).containsOnlyKeys("reportsEnabled", "goalsEnabled");
        assertThat(diff.getValue().get("reportsEnabled")).containsExactly("true", "false");
    }

    @Test
    void update_semMudancaEfetiva_naoSalvaNemRegistraHistorico() {
        when(repository.findByUserId(1L)).thenReturn(Optional.of(existing()));

        service.update(1L, new UserSettingsRequest(true, true, null, null, null, null, null, null));

        verify(repository, never()).save(any());
        verify(historyService, never()).recordChanges(any(), any(), any(), any());
    }

    @Test
    void update_campoNulo_mantemValorAtual() {
        UserSettings settings = existing();
        settings.setEmailsEnabled(false);
        when(repository.findByUserId(1L)).thenReturn(Optional.of(settings));

        UserSettingsResponse result = service.update(1L,
                new UserSettingsRequest(null, null, null, null, null, null, null, null));

        assertThat(result.emailsEnabled()).isFalse();
        verify(repository, never()).save(any());
    }

    // ── leitura dos toggles ──────────────────────────────────────────────────

    @Test
    void toggles_usuarioSemRegistro_tudoHabilitadoPorPadrao() {
        when(repository.findByUserId(9L)).thenReturn(Optional.empty());

        assertThat(service.goalsEnabled(9L)).isTrue();
        assertThat(service.finnyEnabled(9L)).isTrue();
        assertThat(service.emailsEnabled(9L)).isTrue();
        assertThat(service.localesEnabled(9L)).isTrue();
        assertThat(service.institutionsEnabled(9L)).isTrue();
        verify(repository, never()).save(any());
    }

    @Test
    void toggles_comRegistro_refletemOSalvo() {
        UserSettings settings = existing();
        settings.setFinnyEnabled(false);
        settings.setLocalesEnabled(false);
        when(repository.findByUserId(1L)).thenReturn(Optional.of(settings));

        assertThat(service.finnyEnabled(1L)).isFalse();
        assertThat(service.localesEnabled(1L)).isFalse();
        assertThat(service.goalsEnabled(1L)).isTrue();
    }

    // ── find ─────────────────────────────────────────────────────────────────

    @Test
    void find_devolveResponseComOsValoresSalvos() {
        UserSettings settings = existing();
        settings.setBudgetsEnabled(false);
        when(repository.findByUserId(1L)).thenReturn(Optional.of(settings));

        UserSettingsResponse result = service.find(1L);

        assertThat(result.budgetsEnabled()).isFalse();
        assertThat(result.reportsEnabled()).isTrue();
    }
}
