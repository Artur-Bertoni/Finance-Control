package com.financecontrol.service;

import com.financecontrol.entity.Account;
import com.financecontrol.entity.Category;
import com.financecontrol.enums.AccountType;
import com.financecontrol.repository.AccountRepository;
import com.financecontrol.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock CategoryRepository categoryRepository;
    @Mock AccountRepository accountRepository;

    @InjectMocks OnboardingService onboardingService;

    @Test
    void seedDefaults_usuarioNovo_semeiaCategoriasEContaCarteira() {
        when(categoryRepository.countByUserId(1L)).thenReturn(0L);
        when(accountRepository.countByUserId(1L)).thenReturn(0L);

        onboardingService.seedDefaults(1L, "pt");

        ArgumentCaptor<Category> catCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository, times(10)).save(catCaptor.capture());
        assertThat(catCaptor.getAllValues())
                .allMatch(c -> c.getUserId().equals(1L))
                .allMatch(Category::isSeeded)
                .allMatch(c -> c.getName() != null && !c.getName().isBlank())
                .allMatch(c -> !c.getAliases().isEmpty());

        ArgumentCaptor<Account> accCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accCaptor.capture());
        Account wallet = accCaptor.getValue();
        assertThat(wallet.getName()).isEqualTo("Carteira");
        assertThat(wallet.isSeeded()).isTrue();
        assertThat(wallet.getType()).isEqualTo(AccountType.CHECKING);
        assertThat(wallet.getBalance()).isZero();
    }

    @Test
    void seedDefaults_idiomaIngles_usaNomesEmIngles() {
        when(categoryRepository.countByUserId(2L)).thenReturn(0L);
        when(accountRepository.countByUserId(2L)).thenReturn(0L);

        onboardingService.seedDefaults(2L, "en");

        ArgumentCaptor<Account> accCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accCaptor.capture());
        assertThat(accCaptor.getValue().getName()).isEqualTo("Wallet");
    }

    @Test
    void seedDefaults_usuarioJaTemDados_naoSemeia() {
        when(categoryRepository.countByUserId(3L)).thenReturn(5L);

        onboardingService.seedDefaults(3L, "pt");

        verify(categoryRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void seedDefaults_userIdNulo_naoFazNada() {
        onboardingService.seedDefaults(null, "pt");

        verifyNoInteractions(categoryRepository, accountRepository);
    }
}
