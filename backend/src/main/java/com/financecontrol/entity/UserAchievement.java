package com.financecontrol.entity;

import com.financecontrol.enums.AchievementType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_achievement")
@Getter @Setter @NoArgsConstructor
public class UserAchievement {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "achievement_type")
    private AchievementType achievementType;

    @Column(name = "earned_at")
    private LocalDateTime earnedAt;

    public UserAchievement(Long userId, AchievementType type) {
        this.userId          = userId;
        this.achievementType = type;
        this.earnedAt        = LocalDateTime.now();
    }
}
