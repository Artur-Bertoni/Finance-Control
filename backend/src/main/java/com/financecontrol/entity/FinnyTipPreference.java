package com.financecontrol.entity;

import com.financecontrol.enums.FinnyTipCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * O "aprendizado" do agente por usuário: um peso por categoria de dica.
 * Começa em 1.0 e é ajustado pelo feedback (explícito e implícito). Quanto maior
 * o peso, mais o agente prioriza dicas daquela categoria para este usuário.
 */
@Entity
@Table(name = "finny_tip_preference",
       uniqueConstraints = @UniqueConstraint(name = "uq_finny_pref_user_category",
                                             columnNames = {"user_id", "category"}))
@Getter @Setter @NoArgsConstructor
public class FinnyTipPreference {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30)
    private FinnyTipCategory category;

    @Column(name = "weight")
    private double weight = 1.0;

    @Column(name = "helpful_count")
    private int helpfulCount = 0;

    @Column(name = "not_helpful_count")
    private int notHelpfulCount = 0;

    @Column(name = "dismissed_count")
    private int dismissedCount = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
