package com.financecontrol.repository;

import com.financecontrol.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t " +
           "WHERE t.userId = :userId " +
           "AND t.date BETWEEN :startDate AND :endDate " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:accountId IS NULL OR t.account.id = :accountId) " +
           "ORDER BY t.date DESC, t.id DESC")
    List<Transaction> findAllFiltered(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryId") Long categoryId,
            @Param("accountId") Long accountId);

    @Query("SELECT YEAR(t.date), MONTH(t.date), t.type, SUM(t.value) " +
           "FROM Transaction t " +
           "WHERE t.userId = :userId " +
           "AND t.date BETWEEN :startDate AND :endDate " +
           "AND (t.transferPartnerId IS NULL OR t.transferPartnerId = 0) " +
           "AND (:accountId IS NULL OR t.account.id = :accountId) " +
           "GROUP BY YEAR(t.date), MONTH(t.date), t.type " +
           "ORDER BY YEAR(t.date), MONTH(t.date)")
    List<Object[]> findMonthlyTotals(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("accountId") Long accountId);

    @Query("SELECT t.category.id, t.category.name, t.type, SUM(t.value) " +
           "FROM Transaction t " +
           "WHERE t.userId = :userId " +
           "AND t.date BETWEEN :startDate AND :endDate " +
           "AND (t.transferPartnerId IS NULL OR t.transferPartnerId = 0) " +
           "AND (:accountId IS NULL OR t.account.id = :accountId) " +
           "GROUP BY t.category.id, t.category.name, t.type")
    List<Object[]> findCategoryTotals(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("accountId") Long accountId);
}
