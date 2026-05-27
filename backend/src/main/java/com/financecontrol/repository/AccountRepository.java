package com.financecontrol.repository;

import com.financecontrol.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserIdOrderByNameAsc(Long userId);

    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);

    long countByUserId(Long userId);

    @Query("SELECT COUNT(DISTINCT a.financialInstitution.id) FROM Account a WHERE a.userId = :userId AND a.financialInstitution IS NOT NULL")
    long countDistinctInstitutionsByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.userId = :userId AND (:accountId IS NULL OR a.id = :accountId)")
    Double sumBalance(@Param("userId") Long userId, @Param("accountId") Long accountId);

    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance + :delta WHERE a.id = :id")
    void patchBalance(@Param("id") Long id, @Param("delta") Double delta);
}
