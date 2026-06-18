package com.financecontrol.repository;

import com.financecontrol.entity.EntityChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntityChangeLogRepository extends JpaRepository<EntityChangeLog, Long> {
    List<EntityChangeLog> findByEntityTypeAndEntityIdOrderByChangedAtDesc(String entityType, Long entityId);
}
