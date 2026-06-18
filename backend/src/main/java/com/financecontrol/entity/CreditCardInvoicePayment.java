package com.financecontrol.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_card_invoice_payment",
       uniqueConstraints = @UniqueConstraint(name = "uq_invoice_payment_account_ref",
                                             columnNames = {"account_id", "reference_month"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreditCardInvoicePayment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "reference_month")
    private String referenceMonth;

    @Column(name = "value")
    private Double value;

    @Column(name = "source_account_id")
    private Long sourceAccountId;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
