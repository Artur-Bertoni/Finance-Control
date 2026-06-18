package com.financecontrol.repository;

import com.financecontrol.entity.FinnyTipPreference;
import com.financecontrol.enums.FinnyTipCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinnyTipPreferenceRepository extends JpaRepository<FinnyTipPreference, Long> {

    List<FinnyTipPreference> findByUserId(Long userId);

    Optional<FinnyTipPreference> findByUserIdAndCategory(Long userId, FinnyTipCategory category);
}
