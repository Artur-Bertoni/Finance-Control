package com.financecontrol.repository;

import com.financecontrol.entity.FinnyTipPreference;
import com.financecontrol.enums.FinnyTipCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinnyTipPreferenceRepository extends JpaRepository<FinnyTipPreference, Long> {

    List<FinnyTipPreference> findByUserId(Long userId);

    Optional<FinnyTipPreference> findByUserIdAndCategory(Long userId, FinnyTipCategory category);
}
