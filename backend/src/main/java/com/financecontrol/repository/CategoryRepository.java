package com.financecontrol.repository;

import com.financecontrol.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserIdOrderByIdDesc(Long userId);
    java.util.Optional<Category> findByUserIdAndInternalName(Long userId, String internalName);
}
