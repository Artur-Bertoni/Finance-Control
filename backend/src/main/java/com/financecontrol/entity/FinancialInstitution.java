package com.financecontrol.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_institution")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FinancialInstitution {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String name;
    private String address;
    private String contact;

    @Column(name = "icon_key")
    private String iconKey;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
