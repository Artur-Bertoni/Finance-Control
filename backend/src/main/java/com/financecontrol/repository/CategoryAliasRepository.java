package com.financecontrol.repository;

import com.financecontrol.entity.CategoryAlias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryAliasRepository extends JpaRepository<CategoryAlias, Long> {
    Optional<CategoryAlias> findByCategoryUserIdAndAliasName(Long userId, String aliasName);
}
