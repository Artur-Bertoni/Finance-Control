package com.financecontrol.repository;

import com.financecontrol.entity.TransactionLocale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionLocaleRepository extends JpaRepository<TransactionLocale, Long> {
    List<TransactionLocale> findByUserIdOrderByIdDesc(Long userId);
}
