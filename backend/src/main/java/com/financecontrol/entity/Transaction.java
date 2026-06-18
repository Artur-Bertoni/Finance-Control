package com.financecontrol.entity;

import com.financecontrol.config.TransactionTypeConverter;
import com.financecontrol.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_locale_id")
    private TransactionLocale transactionLocale;

    private Double value;
    private LocalDate date;

    @Convert(converter = TransactionTypeConverter.class)
    private TransactionType type;

    @Column(name = "installments_number")
    private Integer installmentsNumber;

    private String obs;

    @Column(name = "transfer_partner_id")
    private Long transferPartnerId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "installment_group_id")
    private Long installmentGroupId;

    @Column(name = "installment_index")
    private Integer installmentIndex;

    @Column(name = "applied")
    private Boolean applied;
}
