package com.financecontrol.repository;

import com.financecontrol.entity.UserAchievement;
import com.financecontrol.enums.AchievementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface AchievementRepository extends JpaRepository<UserAchievement, Long> {

    List<UserAchievement> findByUserId(Long userId);

    boolean existsByUserIdAndAchievementType(Long userId, AchievementType type);

    default Set<AchievementType> earnedSet(Long userId) {
        return findByUserId(userId).stream()
                .map(UserAchievement::getAchievementType)
                .collect(Collectors.toSet());
    }
}
