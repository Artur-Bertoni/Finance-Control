package com.financecontrol.repository;

import com.financecontrol.entity.CategoryAlias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryAliasRepository extends JpaRepository<CategoryAlias, Long> {
    Optional<CategoryAlias> findFirstByCategoryUserIdAndAliasName(Long userId, String aliasName);
    List<CategoryAlias> findAllByCategoryUserIdAndAliasName(Long userId, String aliasName);
}
