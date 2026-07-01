package com.financecontrol.service;

import com.financecontrol.dto.request.CategoryRequest;
import com.financecontrol.dto.response.CategoryResponse;
import com.financecontrol.entity.Category;
import com.financecontrol.entity.CategoryAlias;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.CategoryAliasRepository;
import com.financecontrol.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock CategoryRepository categoryRepository;
    @Mock CategoryAliasRepository categoryAliasRepository;
    @Mock HistoryService historyService;
    @Mock EntityManager entityManager;

    @InjectMocks CategoryService categoryService;

    @BeforeEach
    void injectEntityManager() {
        ReflectionTestUtils.setField(categoryService, "entityManager", entityManager);
    }

    // ── findAllByUser ────────────────────────────────────────────────────────

    @Test
    void findAllByUser_retornaCategorias() {
        Category c = categoryWith(1L, 1L, "Alimentação", null, null);
        when(categoryRepository.findByUserIdWithAliases(1L)).thenReturn(List.of(c));

        List<CategoryResponse> result = categoryService.findAllByUser(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Alimentação");
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    void findById_encontrado_retornaResponse() {
        when(categoryRepository.findByIdWithAliases(1L))
                .thenReturn(Optional.of(categoryWith(1L, 1L, "Transporte", "Ônibus/metrô", "ph-bus")));

        CategoryResponse result = categoryService.findById(1L, 1L);

        assertThat(result.name()).isEqualTo("Transporte");
        assertThat(result.description()).isEqualTo("Ônibus/metrô");
    }

    @Test
    void findById_naoEncontrado_lancaResourceNotFoundException() {
        when(categoryRepository.findByIdWithAliases(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.findById(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void create_semAliases_usaNomeComoAlias() {
        CategoryRequest req = new CategoryRequest("Lazer", null, null, null);
        Category saved = categoryWith(10L, 1L, "Lazer", null, null);

        when(categoryRepository.existsByUserIdAndNameIgnoreCase(1L, "Lazer")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse result = categoryService.create(1L, req, false);

        assertThat(result.name()).isEqualTo("Lazer");
        verify(historyService).recordCreation(HistoryService.ENTITY_CATEGORY, 10L, 1L);
    }

    @Test
    void create_comAliases_salvaAliasesInformados() {
        CategoryRequest req = new CategoryRequest("Alimentação", null, null, List.of("Comida", "Aliment"));
        Category saved = categoryWith(11L, 1L, "Alimentação", null, null);

        when(categoryRepository.existsByUserIdAndNameIgnoreCase(1L, "Alimentação")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse result = categoryService.create(1L, req, false);

        assertThat(result.name()).isEqualTo("Alimentação");
        verify(historyService).recordCreation(HistoryService.ENTITY_CATEGORY, 11L, 1L);
    }

    @Test
    void create_nomeDuplicado_lancaBusinessException() {
        CategoryRequest req = new CategoryRequest("Lazer", null, null, null);
        when(categoryRepository.existsByUserIdAndNameIgnoreCase(1L, "Lazer")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(1L, req, false))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void create_forceTrue_ignoraDuplicadoESalva() {
        CategoryRequest req = new CategoryRequest("Lazer", null, null, null);
        Category saved = categoryWith(12L, 1L, "Lazer", null, null);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        assertThatCode(() -> categoryService.create(1L, req, true)).doesNotThrowAnyException();
        verify(categoryRepository, never()).existsByUserIdAndNameIgnoreCase(any(), any());
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_encontrado_atualizaERegistraDiff() {
        Category existing = categoryWith(1L, 1L, "Antigo", null, null);
        Category saved = categoryWith(1L, 1L, "Novo", "desc", "ph-tag");

        when(categoryRepository.findByIdWithAliases(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse result = categoryService.update(1L, 1L,
                new CategoryRequest("Novo", "desc", "ph-tag", List.of("Novo")));

        assertThat(result.name()).isEqualTo("Novo");
        verify(historyService).recordChanges(eq(HistoryService.ENTITY_CATEGORY), eq(1L), eq(1L), any());
    }

    @Test
    void update_naoEncontrado_lancaResourceNotFoundException() {
        when(categoryRepository.findByIdWithAliases(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.update(99L, 1L,
                new CategoryRequest("X", null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── findByAlias ──────────────────────────────────────────────────────────

    @Test
    void findByAlias_retornaCategorias() {
        Category c = categoryWith(1L, 1L, "Alimentação", null, null);
        CategoryAlias alias = new CategoryAlias(c, "Comida");
        when(categoryAliasRepository.findAllByCategoryUserIdAndAliasName(1L, "Comida"))
                .thenReturn(List.of(alias));

        List<Category> result = categoryService.findByAlias(1L, "Comida");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Alimentação");
    }

    // ── learnAlias ───────────────────────────────────────────────────────────

    @Test
    void learnAlias_novaMapeamento_salvaNoBanco() {
        Category c = categoryWith(5L, 1L, "Alimentação", null, null);
        when(categoryAliasRepository.findAllByCategoryUserIdAndAliasName(1L, "Mercado"))
                .thenReturn(List.of());
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(c));

        categoryService.learnAlias(1L, "Mercado", 5L);

        verify(categoryAliasRepository).save(any(CategoryAlias.class));
    }

    @Test
    void learnAlias_jaExisteMapeamento_naoSalvaNovamente() {
        Category c = categoryWith(5L, 1L, "Alimentação", null, null);
        CategoryAlias existing = new CategoryAlias(c, "Mercado");
        when(categoryAliasRepository.findAllByCategoryUserIdAndAliasName(1L, "Mercado"))
                .thenReturn(List.of(existing));

        categoryService.learnAlias(1L, "Mercado", 5L);

        verify(categoryAliasRepository, never()).save(any());
    }

    @Test
    void learnAlias_aliasNuloOuBranco_retornaSemSalvar() {
        categoryService.learnAlias(1L, null, 5L);
        categoryService.learnAlias(1L, "   ", 5L);
        verify(categoryAliasRepository, never()).findAllByCategoryUserIdAndAliasName(any(), any());
    }

    @Test
    void learnAlias_categoriaNaoEncontrada_lancaResourceNotFoundException() {
        when(categoryAliasRepository.findAllByCategoryUserIdAndAliasName(1L, "X"))
                .thenReturn(List.of());
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.learnAlias(1L, "X", 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    void delete_encontrado_deletaDoRepositorio() {
        when(categoryRepository.findByIdWithAliases(1L))
                .thenReturn(Optional.of(categoryWith(1L, 1L, "X", null, null)));

        categoryService.delete(1L, 1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void delete_naoEncontrado_lancaResourceNotFoundException() {
        when(categoryRepository.findByIdWithAliases(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.delete(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── helper ───────────────────────────────────────────────────────────────

    private static Category categoryWith(Long id, Long userId, String name,
                                         String description, String iconKey) {
        Category c = new Category();
        c.setId(id);
        c.setUserId(userId);
        c.setName(name);
        c.setDescription(description);
        c.setIconKey(iconKey);
        return c;
    }
}
