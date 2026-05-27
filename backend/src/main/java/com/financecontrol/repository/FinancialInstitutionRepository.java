package com.financecontrol.repository;

import com.financecontrol.entity.FinancialInstitution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialInstitutionRepository extends JpaRepository<FinancialInstitution, Long> {
    List<FinancialInstitution> findByUserIdOrderByNameAsc(Long userId);

    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
}
