package com.financecontrol.service;

import com.financecontrol.entity.Account;
import com.financecontrol.entity.Category;
import com.financecontrol.enums.BulkEntityType;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkOwnershipResolverTest {

    @Mock TransactionRepository          transactionRepository;
    @Mock AccountRepository              accountRepository;
    @Mock CategoryRepository             categoryRepository;
    @Mock TransactionLocaleRepository    transactionLocaleRepository;
    @Mock FinancialInstitutionRepository financialInstitutionRepository;
    @Mock GoalRepository                 goalRepository;
    @Mock BudgetRepository               budgetRepository;

    @InjectMocks BulkOwnershipResolver resolver;

    private static Category category(Long id, Long userId) {
        Category c = new Category();
        c.setId(id);
        c.setUserId(userId);
        return c;
    }

    private static Account account(Long id, Long userId) {
        Account a = new Account();
        a.setId(id);
        a.setUserId(userId);
        return a;
    }

    @Test
    void ownedIds_descartaItensDeOutroUsuario() {
        when(categoryRepository.findAllById(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(category(1L, 7L), category(2L, 99L), category(3L, 7L)));

        assertThat(resolver.ownedIds(BulkEntityType.CATEGORIES, List.of(1L, 2L, 3L), 7L))
                .containsExactly(1L, 3L);
    }

    @Test
    void ownedIds_removeDuplicadosENulos() {
        when(accountRepository.findAllById(List.of(5L))).thenReturn(List.of(account(5L, 7L)));

        List<Long> ids = java.util.Arrays.asList(5L, 5L, null);

        assertThat(resolver.ownedIds(BulkEntityType.ACCOUNTS, ids, 7L)).containsExactly(5L);
    }

    @Test
    void ownedIds_listaVaziaOuNulaRetornaVazio() {
        assertThat(resolver.ownedIds(BulkEntityType.GOALS, List.of(), 7L)).isEmpty();
        assertThat(resolver.ownedIds(BulkEntityType.GOALS, null, 7L)).isEmpty();
        verifyNoInteractions(goalRepository);
    }

    @Test
    void ownedIds_acimaDoLimiteLancaBusinessException() {
        List<Long> ids = LongStream.rangeClosed(1, 501).boxed().toList();

        assertThatThrownBy(() -> resolver.ownedIds(BulkEntityType.CATEGORIES, ids, 7L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.bulk.tooManyItems");
    }
}
