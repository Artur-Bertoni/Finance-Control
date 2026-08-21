package com.financecontrol.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings",
       uniqueConstraints = @UniqueConstraint(name = "uq_user_settings_user",
                                             columnNames = "user_id"))
@Getter @Setter @NoArgsConstructor
public class UserSettings {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "reports_enabled")
    private boolean reportsEnabled = true;

    @Column(name = "budgets_enabled")
    private boolean budgetsEnabled = true;

    @Column(name = "goals_enabled")
    private boolean goalsEnabled = true;

    @Column(name = "finny_enabled")
    private boolean finnyEnabled = true;

    @Column(name = "statement_import_enabled")
    private boolean statementImportEnabled = true;

    @Column(name = "institutions_enabled")
    private boolean institutionsEnabled = true;

    @Column(name = "locales_enabled")
    private boolean localesEnabled = true;

    @Column(name = "emails_enabled")
    private boolean emailsEnabled = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
