package com.financecontrol.repository;

import com.financecontrol.entity.FinancialInstitution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinancialInstitutionRepository extends JpaRepository<FinancialInstitution, Long> {
    List<FinancialInstitution> findByUserIdOrderByNameAsc(Long userId);

    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
}
