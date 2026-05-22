package com.financecontrol.repository;

import com.financecontrol.entity.CategoryAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryAliasRepository extends JpaRepository<CategoryAlias, Long> {
    Optional<CategoryAlias> findFirstByCategoryUserIdAndAliasName(Long userId, String aliasName);
}
