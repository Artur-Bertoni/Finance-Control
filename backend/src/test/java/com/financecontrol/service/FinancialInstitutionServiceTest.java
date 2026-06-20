package com.financecontrol.service;

import com.financecontrol.dto.request.FinancialInstitutionRequest;
import com.financecontrol.dto.response.FinancialInstitutionResponse;
import com.financecontrol.entity.FinancialInstitution;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.FinancialInstitutionRepository;
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
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class FinancialInstitutionServiceTest {

    @Mock FinancialInstitutionRepository financialInstitutionRepository;
    @Mock HistoryService historyService;

    @InjectMocks FinancialInstitutionService financialInstitutionService;

    // ── findAllByUser ────────────────────────────────────────────────────────

    @Test
    void findAllByUser_retornaInstituicoes() {
        FinancialInstitution fi = fi(1L, 1L, "Nubank", null, null, null);
        when(financialInstitutionRepository.findByUserIdOrderByNameAsc(1L)).thenReturn(List.of(fi));

        List<FinancialInstitutionResponse> result = financialInstitutionService.findAllByUser(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Nubank");
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    void findById_encontrado_retornaResponse() {
        when(financialInstitutionRepository.findById(1L))
                .thenReturn(Optional.of(fi(1L, 1L, "Itaú", "Av P", "11 9999", "ph-bank")));

        FinancialInstitutionResponse result = financialInstitutionService.findById(1L);

        assertThat(result.name()).isEqualTo("Itaú");
        assertThat(result.contact()).isEqualTo("11 9999");
    }

    @Test
    void findById_naoEncontrado_lancaResourceNotFoundException() {
        when(financialInstitutionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> financialInstitutionService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void create_sucesso_registraHistoricoERetornaResponse() {
        FinancialInstitutionRequest req = new FinancialInstitutionRequest("Bradesco", null, null, null);
        FinancialInstitution saved = fi(5L, 1L, "Bradesco", null, null, null);

        when(financialInstitutionRepository.existsByUserIdAndNameIgnoreCase(1L, "Bradesco")).thenReturn(false);
        when(financialInstitutionRepository.save(any(FinancialInstitution.class))).thenReturn(saved);

        FinancialInstitutionResponse result = financialInstitutionService.create(1L, req, false);

        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.name()).isEqualTo("Bradesco");
        verify(historyService).recordCreation(HistoryService.ENTITY_INSTITUTION, 5L, 1L);
    }

    @Test
    void create_nomeDuplicado_lancaBusinessException() {
        FinancialInstitutionRequest req = new FinancialInstitutionRequest("Bradesco", null, null, null);
        when(financialInstitutionRepository.existsByUserIdAndNameIgnoreCase(1L, "Bradesco")).thenReturn(true);

        assertThatThrownBy(() -> financialInstitutionService.create(1L, req, false))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void create_forceTrue_ignoraDuplicadoESalva() {
        FinancialInstitutionRequest req = new FinancialInstitutionRequest("Bradesco", null, null, null);
        FinancialInstitution saved = fi(6L, 1L, "Bradesco", null, null, null);
        when(financialInstitutionRepository.save(any(FinancialInstitution.class))).thenReturn(saved);

        assertThatCode(() -> financialInstitutionService.create(1L, req, true)).doesNotThrowAnyException();
        verify(financialInstitutionRepository, never()).existsByUserIdAndNameIgnoreCase(any(), any());
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_alteraNome_registraDiffERetornaResponse() {
        FinancialInstitution existing = fi(1L, 1L, "Antigo", null, null, null);
        FinancialInstitution saved = fi(1L, 1L, "Novo", null, null, null);

        when(financialInstitutionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(financialInstitutionRepository.save(any(FinancialInstitution.class))).thenReturn(saved);

        FinancialInstitutionResponse result = financialInstitutionService.update(1L,
                new FinancialInstitutionRequest("Novo", null, null, null));

        assertThat(result.name()).isEqualTo("Novo");
        verify(historyService).recordChanges(eq(HistoryService.ENTITY_INSTITUTION), eq(1L), eq(1L), any());
    }

    @Test
    void update_semMudancas_registraDiffVazio() {
        FinancialInstitution existing = fi(1L, 1L, "Igual", "Rua A", "11", "ph-bank");
        when(financialInstitutionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(financialInstitutionRepository.save(any(FinancialInstitution.class))).thenReturn(existing);

        financialInstitutionService.update(1L, new FinancialInstitutionRequest("Igual", "Rua A", "11", "ph-bank"));

        verify(historyService).recordChanges(any(), any(), any(), any());
    }

    @Test
    void update_naoEncontrado_lancaResourceNotFoundException() {
        when(financialInstitutionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> financialInstitutionService.update(99L,
                new FinancialInstitutionRequest("X", null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    void delete_encontrado_deletaDoRepositorio() {
        when(financialInstitutionRepository.findById(1L))
                .thenReturn(Optional.of(fi(1L, 1L, "X", null, null, null)));

        financialInstitutionService.delete(1L);

        verify(financialInstitutionRepository).deleteById(1L);
    }

    @Test
    void delete_naoEncontrado_lancaResourceNotFoundException() {
        when(financialInstitutionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> financialInstitutionService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── update per-field buildDiff branch coverage ──────────────────────────

    @Test
    void update_mudaApenasEndereco_registraDiffAddress() {
        FinancialInstitution existing = fi(1L, 1L, "Banco", "Rua A", "11 9999", "ph-bank");
        when(financialInstitutionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(financialInstitutionRepository.save(any(FinancialInstitution.class))).thenAnswer(inv -> inv.getArgument(0));

        financialInstitutionService.update(1L, new FinancialInstitutionRequest("Banco", "Rua B", "11 9999", "ph-bank"));

        verify(historyService).recordChanges(eq(HistoryService.ENTITY_INSTITUTION), eq(1L), eq(1L), any());
    }

    @Test
    void update_mudaApenasContato_registraDiffContact() {
        FinancialInstitution existing = fi(1L, 1L, "Banco", "Rua A", "11 9999", "ph-bank");
        when(financialInstitutionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(financialInstitutionRepository.save(any(FinancialInstitution.class))).thenAnswer(inv -> inv.getArgument(0));

        financialInstitutionService.update(1L, new FinancialInstitutionRequest("Banco", "Rua A", "11 0000", "ph-bank"));

        verify(historyService).recordChanges(eq(HistoryService.ENTITY_INSTITUTION), eq(1L), eq(1L), any());
    }

    @Test
    void update_mudaApenasIconKey_registraDiffIconKey() {
        FinancialInstitution existing = fi(1L, 1L, "Banco", "Rua A", "11 9999", "ph-bank");
        when(financialInstitutionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(financialInstitutionRepository.save(any(FinancialInstitution.class))).thenAnswer(inv -> inv.getArgument(0));

        FinancialInstitutionResponse result = financialInstitutionService.update(1L,
                new FinancialInstitutionRequest("Banco", "Rua A", "11 9999", "ph-wallet"));

        assertThat(result.iconKey()).isEqualTo("ph-wallet");
        verify(historyService).recordChanges(eq(HistoryService.ENTITY_INSTITUTION), eq(1L), eq(1L), any());
    }

    @Test
    void update_mudaTodosOsCampos_aplicaTodosNoEntity() {
        FinancialInstitution existing = fi(1L, 1L, "Velho", "Rua A", "11 9999", "ph-bank");
        when(financialInstitutionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(financialInstitutionRepository.save(any(FinancialInstitution.class))).thenAnswer(inv -> inv.getArgument(0));

        FinancialInstitutionResponse result = financialInstitutionService.update(1L,
                new FinancialInstitutionRequest("Novo", "Rua B", "11 0000", "ph-wallet"));

        assertThat(result.name()).isEqualTo("Novo");
        assertThat(result.address()).isEqualTo("Rua B");
        assertThat(result.contact()).isEqualTo("11 0000");
        assertThat(result.iconKey()).isEqualTo("ph-wallet");
    }

    // ── helper ───────────────────────────────────────────────────────────────

    private static FinancialInstitution fi(Long id, Long userId, String name,
                                           String address, String contact, String iconKey) {
        return new FinancialInstitution(id, userId, name, address, contact, iconKey, LocalDateTime.now());
    }
}
