package com.financecontrol.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "category_alias")
@Getter @Setter @NoArgsConstructor
public class CategoryAlias {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "alias_name")
    private String aliasName;

    public CategoryAlias(Category category, String aliasName) {
        this.category = category;
        this.aliasName = aliasName;
    }
}
