package com.financecontrol.entity;

import com.financecontrol.enums.GoalNotificationType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal_notification_log")
@Getter @Setter @NoArgsConstructor
public class GoalNotificationLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "goal_id")
    private Long goalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type")
    private GoalNotificationType notificationType;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
