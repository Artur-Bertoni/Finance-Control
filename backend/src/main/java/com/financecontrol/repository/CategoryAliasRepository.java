package com.financecontrol.repository;

import com.financecontrol.entity.CategoryAlias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryAliasRepository extends JpaRepository<CategoryAlias, Long> {
    List<CategoryAlias> findAllByCategoryUserIdAndAliasName(Long userId, String aliasName);
}
