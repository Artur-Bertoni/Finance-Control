package com.financecontrol.repository;

import com.financecontrol.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmailAndIdNot(String email, Long id);
    List<User> findByEmailNotificationEnabledTrueAndEmailNotificationDay(int day);
}
