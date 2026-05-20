package com.financecontrol.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.lang.NonNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "entity_change_log")
@Getter @Setter @NoArgsConstructor
public class EntityChangeLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "old_value", length = 500)
    private String oldValue;

    @Column(name = "new_value", length = 500)
    private String newValue;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "group_id", nullable = false, length = 36)
    private String groupId;

    @NonNull
    public static EntityChangeLog of(String entityType, Long entityId, Long userId,
                                     String fieldName, String oldValue, String newValue,
                                     LocalDateTime changedAt, String groupId) {
        EntityChangeLog log = new EntityChangeLog();
        log.entityType = entityType;
        log.entityId   = entityId;
        log.userId     = userId;
        log.fieldName  = fieldName;
        log.oldValue   = oldValue;
        log.newValue   = newValue;
        log.changedAt  = changedAt;
        log.groupId    = groupId;
        return log;
    }
}
