package com.financecontrol.entity;

import com.financecontrol.enums.GoalStatus;
import com.financecontrol.enums.GoalType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "financial_goal")
@Getter @Setter @NoArgsConstructor
public class Goal {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private GoalType type;

    @Enumerated(EnumType.STRING)
    private GoalStatus status = GoalStatus.ACTIVE;

    @Column(name = "target_amount")
    private Double targetAmount;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "goal_category",
        joinColumns = @JoinColumn(name = "goal_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "goal_locale",
        joinColumns = @JoinColumn(name = "goal_id"),
        inverseJoinColumns = @JoinColumn(name = "locale_id")
    )
    private List<TransactionLocale> locales = new ArrayList<>();

    @Column(name = "notify_at_50")
    private Boolean notifyAt50 = true;

    @Column(name = "notify_at_75")
    private Boolean notifyAt75 = true;

    @Column(name = "notify_at_90")
    private Boolean notifyAt90 = true;

    @Column(name = "notify_on_complete")
    private Boolean notifyOnComplete = true;

    @Column(name = "notify_on_deadline")
    private Boolean notifyOnDeadline = true;

    @Column(name = "notify_on_exceed")
    private Boolean notifyOnExceed = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
