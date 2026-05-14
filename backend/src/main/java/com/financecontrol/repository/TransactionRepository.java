package com.financecontrol.repository;

import com.financecontrol.entity.Transaction;
import com.financecontrol.enums.TransactionType;
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

    @Query("SELECT COALESCE(SUM(t.value), 0.0) FROM Transaction t " +
           "WHERE t.userId = :userId AND t.date BETWEEN :startDate AND :endDate " +
           "AND t.type = :type AND (t.transferPartnerId IS NULL OR t.transferPartnerId = 0)")
    Double sumForGoal(@Param("userId") Long userId, @Param("startDate") LocalDate startDate,
                      @Param("endDate") LocalDate endDate, @Param("type") TransactionType type);

    @Query("SELECT COALESCE(SUM(t.value), 0.0) FROM Transaction t " +
           "WHERE t.userId = :userId AND t.date BETWEEN :startDate AND :endDate " +
           "AND t.type = :type AND t.category.id IN :categoryIds " +
           "AND (t.transferPartnerId IS NULL OR t.transferPartnerId = 0)")
    Double sumForGoalByCategories(@Param("userId") Long userId, @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate, @Param("type") TransactionType type,
                                  @Param("categoryIds") List<Long> categoryIds);

    @Query("SELECT COALESCE(SUM(t.value), 0.0) FROM Transaction t " +
           "WHERE t.userId = :userId AND t.date BETWEEN :startDate AND :endDate " +
           "AND t.type = :type AND t.transactionLocale.id IN :localeIds " +
           "AND (t.transferPartnerId IS NULL OR t.transferPartnerId = 0)")
    Double sumForGoalByLocales(@Param("userId") Long userId, @Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate, @Param("type") TransactionType type,
                               @Param("localeIds") List<Long> localeIds);

    @Query("SELECT COALESCE(SUM(t.value), 0.0) FROM Transaction t " +
           "WHERE t.userId = :userId AND t.date BETWEEN :startDate AND :endDate " +
           "AND t.type = :type AND t.category.id IN :categoryIds " +
           "AND t.transactionLocale.id IN :localeIds " +
           "AND (t.transferPartnerId IS NULL OR t.transferPartnerId = 0)")
    Double sumForGoalByCategoriesAndLocales(@Param("userId") Long userId, @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate, @Param("type") TransactionType type,
                                            @Param("categoryIds") List<Long> categoryIds,
                                            @Param("localeIds") List<Long> localeIds);
}
