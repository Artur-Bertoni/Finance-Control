package com.financecontrol.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category")
@Getter @Setter @NoArgsConstructor
public class Category {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String name;
    private String description;

    @Column(name = "icon_key")
    private String iconKey;

    @Column(name = "internal_name")
    private String internalName; // legacy column — not set for new records

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoryAlias> aliases = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
