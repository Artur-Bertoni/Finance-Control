package com.financecontrol.service;

import com.financecontrol.dto.request.TransactionLocaleRequest;
import com.financecontrol.dto.response.TransactionLocaleResponse;
import com.financecontrol.entity.TransactionLocale;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.TransactionLocaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class TransactionLocaleServiceTest {

    @Mock TransactionLocaleRepository transactionLocaleRepository;

    @InjectMocks TransactionLocaleService transactionLocaleService;

    // ── findAllByUser ────────────────────────────────────────────────────────

    @Test
    void findAllByUser_retornaLocais() {
        TransactionLocale tl = locale(1L, 1L, "Supermercado", "Rua A", null);
        when(transactionLocaleRepository.findByUserIdOrderByNameAsc(1L)).thenReturn(List.of(tl));

        List<TransactionLocaleResponse> result = transactionLocaleService.findAllByUser(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Supermercado");
    }

    @Test
    void findAllByUser_semLocais_retornaListaVazia() {
        when(transactionLocaleRepository.findByUserIdOrderByNameAsc(1L)).thenReturn(List.of());
        assertThat(transactionLocaleService.findAllByUser(1L)).isEmpty();
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    void findById_encontrado_retornaResponse() {
        when(transactionLocaleRepository.findById(1L))
                .thenReturn(Optional.of(locale(1L, 1L, "Farmácia", "Av B", "ph-pill")));

        TransactionLocaleResponse result = transactionLocaleService.findById(1L);

        assertThat(result.name()).isEqualTo("Farmácia");
        assertThat(result.iconKey()).isEqualTo("ph-pill");
    }

    @Test
    void findById_naoEncontrado_lancaResourceNotFoundException() {
        when(transactionLocaleRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> transactionLocaleService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void create_sucesso_retornaResponse() {
        TransactionLocaleRequest req = new TransactionLocaleRequest("Padaria", "Rua C", null);
        TransactionLocale saved = locale(10L, 1L, "Padaria", "Rua C", null);

        when(transactionLocaleRepository.existsByUserIdAndNameIgnoreCase(1L, "Padaria")).thenReturn(false);
        when(transactionLocaleRepository.save(any(TransactionLocale.class))).thenReturn(saved);

        TransactionLocaleResponse result = transactionLocaleService.create(1L, req, false);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.name()).isEqualTo("Padaria");
    }

    @Test
    void create_nomeDuplicado_lancaBusinessException() {
        TransactionLocaleRequest req = new TransactionLocaleRequest("Padaria", null, null);
        when(transactionLocaleRepository.existsByUserIdAndNameIgnoreCase(1L, "Padaria")).thenReturn(true);

        assertThatThrownBy(() -> transactionLocaleService.create(1L, req, false))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void create_forceTrue_ignoraDuplicadoESalva() {
        TransactionLocaleRequest req = new TransactionLocaleRequest("Padaria", null, null);
        TransactionLocale saved = locale(11L, 1L, "Padaria", null, null);
        when(transactionLocaleRepository.save(any(TransactionLocale.class))).thenReturn(saved);

        assertThatCode(() -> transactionLocaleService.create(1L, req, true)).doesNotThrowAnyException();
        verify(transactionLocaleRepository, never()).existsByUserIdAndNameIgnoreCase(any(), any());
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_encontrado_atualizaCampos() {
        TransactionLocale existing = locale(1L, 1L, "Antigo", "Rua X", null);
        TransactionLocale saved = locale(1L, 1L, "Novo", "Rua Y", "ph-map");

        when(transactionLocaleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(transactionLocaleRepository.save(any(TransactionLocale.class))).thenReturn(saved);

        TransactionLocaleResponse result = transactionLocaleService.update(1L,
                new TransactionLocaleRequest("Novo", "Rua Y", "ph-map"));

        assertThat(result.name()).isEqualTo("Novo");
        assertThat(result.address()).isEqualTo("Rua Y");
    }

    @Test
    void update_naoEncontrado_lancaResourceNotFoundException() {
        when(transactionLocaleRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> transactionLocaleService.update(99L,
                new TransactionLocaleRequest("X", null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    void delete_encontrado_deletaDoRepositorio() {
        when(transactionLocaleRepository.findById(1L))
                .thenReturn(Optional.of(locale(1L, 1L, "X", null, null)));

        transactionLocaleService.delete(1L);

        verify(transactionLocaleRepository).deleteById(1L);
    }

    @Test
    void delete_naoEncontrado_lancaResourceNotFoundException() {
        when(transactionLocaleRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> transactionLocaleService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── helper ───────────────────────────────────────────────────────────────

    private static TransactionLocale locale(Long id, Long userId, String name, String address, String iconKey) {
        return new TransactionLocale(id, userId, name, address, iconKey);
    }
}
