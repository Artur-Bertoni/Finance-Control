package com.financecontrol.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transaction_locale")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TransactionLocale {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String name;
    private String address;

    @Column(name = "icon_key")
    private String iconKey;
}
