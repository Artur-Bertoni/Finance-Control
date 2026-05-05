package com.financecontrol.repository;

import com.financecontrol.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserIdOrderByIdDesc(Long userId);

    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.userId = :userId AND (:accountId IS NULL OR a.id = :accountId)")
    Double sumBalance(@Param("userId") Long userId, @Param("accountId") Long accountId);

    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance + :delta WHERE a.id = :id")
    void patchBalance(@Param("id") Long id, @Param("delta") Double delta);
}
