package com.financecontrol.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification_token")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EmailVerificationToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
