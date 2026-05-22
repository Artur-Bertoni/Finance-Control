package com.financecontrol.repository;

import com.financecontrol.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    long countByUserId(Long userId);

    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.aliases WHERE c.userId = :userId ORDER BY c.id DESC")
    List<Category> findByUserIdWithAliases(@Param("userId") Long userId);

    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.aliases WHERE c.id = :id")
    Optional<Category> findByIdWithAliases(@Param("id") Long id);
}
