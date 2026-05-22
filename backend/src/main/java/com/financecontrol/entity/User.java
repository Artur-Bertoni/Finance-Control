package com.financecontrol.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String password;
    private boolean emailNotificationEnabled;
    private int emailNotificationDay;
    private boolean goalEmailNotificationEnabled;
    private String language;
    private boolean admin;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "email_verified")
    private boolean emailVerified;

    private String provider;

    @Column(name = "provider_id")
    private String providerId;
}
