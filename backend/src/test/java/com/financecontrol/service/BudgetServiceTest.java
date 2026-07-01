package com.financecontrol.service;

import com.financecontrol.dto.request.BudgetRequest;
import com.financecontrol.dto.response.BudgetResponse;
import com.financecontrol.entity.Budget;
import com.financecontrol.entity.Category;
import com.financecontrol.enums.TransactionType;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.BudgetRepository;
import com.financecontrol.repository.CategoryRepository;
import com.financecontrol.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock BudgetRepository budgetRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock TransactionRepository transactionRepository;

    @InjectMocks BudgetService budgetService;

    private Category category(Long id, Long userId) {
        Category c = new Category();
        c.setId(id);
        c.setUserId(userId);
        c.setName("Alimentação");
        c.setIconKey("ph-fork-knife");
        return c;
    }

    private Budget budget(Long id, Long userId, Long categoryId, double limit) {
        Budget b = new Budget();
        b.setId(id);
        b.setUserId(userId);
        b.setCategory(category(categoryId, userId));
        b.setMonthlyLimit(limit);
        return b;
    }

    @Test
    void findAllByUser_calculaGastoEPercentual() {
        when(budgetRepository.findByUserId(1L)).thenReturn(List.of(budget(10L, 1L, 5L, 200.0)));
        when(transactionRepository.sumForGoalByCategories(eq(1L), any(LocalDate.class), any(LocalDate.class),
                eq(TransactionType.DEBIT), eq(List.of(5L)))).thenReturn(50.0);

        List<BudgetResponse> result = budgetService.findAllByUser(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).spent()).isEqualTo(50.0);
        assertThat(result.get(0).monthlyLimit()).isEqualTo(200.0);
        assertThat(result.get(0).percent()).isEqualTo(25.0);
        assertThat(result.get(0).categoryName()).isEqualTo("Alimentação");
    }

    @Test
    void upsert_categoriaSemOrcamento_criaNovo() {
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category(5L, 1L)));
        when(budgetRepository.findByUserIdAndCategory_Id(1L, 5L)).thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> {
            Budget b = inv.getArgument(0);
            b.setId(99L);
            return b;
        });
        when(transactionRepository.sumForGoalByCategories(eq(1L), any(), any(), eq(TransactionType.DEBIT), anyList()))
                .thenReturn(0.0);

        BudgetResponse resp = budgetService.upsert(1L, new BudgetRequest(5L, 300.0));

        assertThat(resp.monthlyLimit()).isEqualTo(300.0);
        verify(budgetRepository).save(argThat(b -> b.getUserId().equals(1L) && b.getCategory().getId().equals(5L)));
    }

    @Test
    void upsert_categoriaJaComOrcamento_atualizaLimite() {
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category(5L, 1L)));
        when(budgetRepository.findByUserIdAndCategory_Id(1L, 5L)).thenReturn(Optional.of(budget(10L, 1L, 5L, 100.0)));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.sumForGoalByCategories(eq(1L), any(), any(), eq(TransactionType.DEBIT), anyList()))
                .thenReturn(0.0);

        BudgetResponse resp = budgetService.upsert(1L, new BudgetRequest(5L, 500.0));

        assertThat(resp.monthlyLimit()).isEqualTo(500.0);
    }

    @Test
    void upsert_limiteInvalido_lancaBusinessException() {
        assertThatThrownBy(() -> budgetService.upsert(1L, new BudgetRequest(5L, 0.0)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void upsert_categoriaDeOutroUsuario_lancaResourceNotFound() {
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category(5L, 2L)));

        assertThatThrownBy(() -> budgetService.upsert(1L, new BudgetRequest(5L, 100.0)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_orcamentoDeOutroUsuario_lancaResourceNotFound() {
        when(budgetRepository.findById(10L)).thenReturn(Optional.of(budget(10L, 2L, 5L, 100.0)));

        assertThatThrownBy(() -> budgetService.delete(1L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(budgetRepository, never()).deleteById(any());
    }
}
