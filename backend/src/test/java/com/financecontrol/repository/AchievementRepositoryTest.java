package com.financecontrol.repository;

import com.financecontrol.entity.UserAchievement;
import com.financecontrol.enums.AchievementType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AchievementRepositoryTest {

    @Test
    void earnedSet_mapeiaTiposDeConquista() {
        AchievementRepository repo = mock(AchievementRepository.class);
        when(repo.findByUserId(1L)).thenReturn(List.of(
                new UserAchievement(1L, AchievementType.FIRST_ACCOUNT),
                new UserAchievement(1L, AchievementType.FIRST_GOAL)));
        when(repo.earnedSet(1L)).thenCallRealMethod();

        Set<AchievementType> result = repo.earnedSet(1L);

        assertThat(result).containsExactlyInAnyOrder(
                AchievementType.FIRST_ACCOUNT, AchievementType.FIRST_GOAL);
    }

    @Test
    void earnedSet_semConquistas_retornaSetVazio() {
        AchievementRepository repo = mock(AchievementRepository.class);
        when(repo.findByUserId(2L)).thenReturn(List.of());
        when(repo.earnedSet(2L)).thenCallRealMethod();

        Set<AchievementType> result = repo.earnedSet(2L);

        assertThat(result).isEmpty();
    }

    @Test
    void earnedSet_tiposDuplicados_deduplica() {
        AchievementRepository repo = mock(AchievementRepository.class);
        when(repo.findByUserId(3L)).thenReturn(List.of(
                new UserAchievement(3L, AchievementType.FIRST_ACCOUNT),
                new UserAchievement(3L, AchievementType.FIRST_ACCOUNT)));
        when(repo.earnedSet(3L)).thenCallRealMethod();

        Set<AchievementType> result = repo.earnedSet(3L);

        assertThat(result).containsExactly(AchievementType.FIRST_ACCOUNT);
    }
}
