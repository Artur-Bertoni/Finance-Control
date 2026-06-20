package com.financecontrol.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "budget")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Budget {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "monthly_limit")
    private Double monthlyLimit;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
