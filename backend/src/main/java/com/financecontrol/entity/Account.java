package com.financecontrol.entity;

import com.financecontrol.enums.AccountType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "account")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_institution_id")
    private FinancialInstitution financialInstitution;

    private String name;
    private String contact;
    private String description;
    private Double balance;

    @Column(name = "icon_key")
    private String iconKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private AccountType type;

    @Column(name = "closing_day")
    private Integer closingDay;

    @Column(name = "due_day")
    private Integer dueDay;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
