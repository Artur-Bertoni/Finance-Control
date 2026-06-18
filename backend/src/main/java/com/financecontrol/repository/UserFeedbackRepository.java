package com.financecontrol.repository;

import com.financecontrol.entity.UserFeedback;
import com.financecontrol.enums.FeedbackType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFeedbackRepository extends JpaRepository<UserFeedback, Long> {
    Page<UserFeedback> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<UserFeedback> findByTypeOrderByCreatedAtDesc(FeedbackType type, Pageable pageable);
}
