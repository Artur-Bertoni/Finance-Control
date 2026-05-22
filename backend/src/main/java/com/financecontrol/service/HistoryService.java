package com.financecontrol.service;

import com.financecontrol.dto.response.ChangeGroupResponse;
import com.financecontrol.dto.response.FieldChangeResponse;
import com.financecontrol.entity.EntityChangeLog;
import com.financecontrol.repository.EntityChangeLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HistoryService {

    public static final String ENTITY_ACCOUNT = "account";
    public static final String ENTITY_TRANSACTION = "transaction";
    public static final String ENTITY_CATEGORY = "category";
    public static final String ENTITY_USER = "user";
    public static final String ENTITY_GOAL = "goal";
    public static final String ENTITY_INSTITUTION  = "financial_institution";

    private static final String FIELD_CREATED = "CREATED";
    private static final String FIELD_PASSWORD_CHANGED = "PASSWORD_CHANGED";

    private final EntityChangeLogRepository repository;

    @Transactional(propagation = Propagation.REQUIRED)
    public void recordChanges(String entityType,
                              Long entityId,
                              Long userId,
                              Map<String, String[]> diff) {
        if (diff == null || diff.isEmpty()) return;

        String groupId    = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        diff.forEach((field, values) ->
            repository.save(EntityChangeLog.of(entityType, entityId, userId, field, values[0], values[1], now, groupId))
        );
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void recordCreation(String entityType,
                               Long entityId,
                               Long userId) {
        String groupId = UUID.randomUUID().toString();
        repository.save(EntityChangeLog.of(entityType, entityId, userId, FIELD_CREATED, null, null, LocalDateTime.now(), groupId));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void recordPasswordChange(Long userId) {
        String groupId = UUID.randomUUID().toString();
        repository.save(EntityChangeLog.of(ENTITY_USER, userId, userId, FIELD_PASSWORD_CHANGED, null, null, LocalDateTime.now(), groupId));
    }

    public List<ChangeGroupResponse> getHistory(String entityType,
                                                Long entityId) {
        List<EntityChangeLog> logs = repository.findByEntityTypeAndEntityIdOrderByChangedAtDesc(entityType, entityId);

        Map<String, List<EntityChangeLog>> grouped = new LinkedHashMap<>();
        for (EntityChangeLog log : logs) {
            grouped.computeIfAbsent(log.getGroupId(), k -> new ArrayList<>()).add(log);
        }

        return grouped.entrySet().stream().map(entry -> {
            List<EntityChangeLog> group  = entry.getValue();
            EntityChangeLog       first  = group.get(0);
            boolean isCreation       = group.stream().anyMatch(l -> FIELD_CREATED.equals(l.getFieldName()));
            boolean isPasswordChange = group.stream().anyMatch(l -> FIELD_PASSWORD_CHANGED.equals(l.getFieldName()));

            List<FieldChangeResponse> changes = (isCreation || isPasswordChange)
                ? List.of()
                : group.stream()
                    .map(l -> new FieldChangeResponse(l.getFieldName(), l.getOldValue(), l.getNewValue()))
                    .toList();

            return new ChangeGroupResponse(entry.getKey(), first.getChangedAt(), isCreation, isPasswordChange, changes);
        }).toList();
    }

    public static boolean differs(Object a,
                                  Object b) {
        return !Objects.equals(a, b);
    }

    public static String[] diff(String oldVal,
                                String newVal) {
        return new String[]{ blankToNull(oldVal), blankToNull(newVal) };
    }

    public static String[] diff(Object oldVal,
                                Object newVal) {
        return new String[]{
            oldVal != null ? oldVal.toString() : null,
            newVal != null ? newVal.toString() : null
        };
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
