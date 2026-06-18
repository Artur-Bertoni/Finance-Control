package com.financecontrol.repository;

import com.financecontrol.entity.TransactionLocale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionLocaleRepository extends JpaRepository<TransactionLocale, Long> {
    List<TransactionLocale> findByUserIdOrderByNameAsc(Long userId);

    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
}
