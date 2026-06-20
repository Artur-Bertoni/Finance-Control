package com.financecontrol.entity;

import com.financecontrol.enums.FinnyTipCategory;
import com.financecontrol.enums.FinnyTipStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "finny_tip")
@Getter @Setter @NoArgsConstructor
public class FinnyTip {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "rule_key", length = 60)
    private String ruleKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30)
    private FinnyTipCategory category;

    @Column(name = "params_json", length = 2000)
    private String paramsJson;

    @Column(name = "severity", length = 20)
    private String severity;

    @Column(name = "score")
    private double score;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private FinnyTipStatus status;

    @Column(name = "lang", length = 10)
    private String lang;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "shown_at")
    private LocalDateTime shownAt;

    @Column(name = "feedback_at")
    private LocalDateTime feedbackAt;
}
