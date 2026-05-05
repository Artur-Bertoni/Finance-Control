package com.financecontrol.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

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
    private String type;

    @Column(name = "installments_number")
    private Integer installmentsNumber;

    private String obs;

    @Column(name = "transfer_partner_id")
    private Long transferPartnerId;
}
