package com.financecontrol.entity;

import com.financecontrol.enums.AppNotificationType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_notification")
@Getter @Setter @NoArgsConstructor
public class AppNotification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private AppNotificationType type;

    @Column(name = "goal_id")
    private Long goalId;

    @Column(name = "goal_name")
    private String goalName;

    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "link")
    private String link;

    @Column(name = "is_read")
    private boolean read = false;

    @Column(name = "message", length = 512)
    private String message;

    @Column(name = "severity", length = 20)
    private String severity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
