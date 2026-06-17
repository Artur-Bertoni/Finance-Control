package com.financecontrol.entity;

import com.financecontrol.enums.FinnyTipCategory;
import com.financecontrol.enums.FinnyTipStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Uma dica concreta gerada pelo agente Finny para um usuário, num instante.
 * Guardamos a CHAVE da regra (rule_key) + os PARÂMETROS em JSON, não o texto pronto —
 * assim o histórico é renderizado no idioma atual do usuário a qualquer momento.
 */
@Entity
@Table(name = "finny_tip")
@Getter @Setter @NoArgsConstructor
public class FinnyTip {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    /** Identifica a regra que gerou a dica (ex: SAVINGS_RATE_LOW). Vira a chave i18n no front. */
    @Column(name = "rule_key", length = 60)
    private String ruleKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30)
    private FinnyTipCategory category;

    /**
     * Parâmetros da dica serializados em JSON (ex: {"pct":15,"category":"Restaurantes"}).
     * Em produção (MySQL) a coluna é TEXT pela migration; aqui usamos um length amplo para
     * que o H2 (usado nos testes) também crie a coluna corretamente. JSON de dica é pequeno.
     */
    @Column(name = "params_json", length = 2000)
    private String paramsJson;

    @Column(name = "severity", length = 20)
    private String severity;

    /** Relevância calculada no momento da geração (score base × peso adaptativo). */
    @Column(name = "score")
    private double score;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private FinnyTipStatus status;

    /** Idioma em que a dica foi gerada (referência; o texto é renderizado no front). */
    @Column(name = "lang", length = 10)
    private String lang;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "shown_at")
    private LocalDateTime shownAt;

    @Column(name = "feedback_at")
    private LocalDateTime feedbackAt;
}
