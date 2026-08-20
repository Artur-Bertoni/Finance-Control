package com.financecontrol.repository;

import com.financecontrol.entity.EntityChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EntityChangeLogRepository extends JpaRepository<EntityChangeLog, Long> {
    List<EntityChangeLog> findByEntityTypeAndEntityIdOrderByChangedAtDesc(String entityType, Long entityId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM EntityChangeLog l WHERE l.entityType = :entityType AND l.entityId IN :entityIds")
    void deleteByEntityTypeAndEntityIdIn(@Param("entityType") String entityType,
                                         @Param("entityIds") List<Long> entityIds);
}
