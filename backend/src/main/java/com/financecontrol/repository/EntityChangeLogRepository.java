package com.financecontrol.repository;

import com.financecontrol.entity.EntityChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntityChangeLogRepository extends JpaRepository<EntityChangeLog, Long> {
    List<EntityChangeLog> findByEntityTypeAndEntityIdOrderByChangedAtDesc(String entityType, Long entityId);
}
