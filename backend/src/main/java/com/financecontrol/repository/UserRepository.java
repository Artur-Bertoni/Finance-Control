package com.financecontrol.repository;

import com.financecontrol.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByIdAndActiveTrue(Long id);
    Optional<User> findByEmailAndActiveTrue(String email);
    Optional<User> findByUsernameAndActiveTrue(String username);
    boolean existsByUsernameAndActiveTrue(String username);
    boolean existsByEmailAndActiveTrueAndIdNot(String email, Long id);
    List<User> findByEmailNotificationEnabledTrueAndEmailNotificationDayAndActiveTrue(int day);
    Optional<User> findByProviderAndProviderIdAndActiveTrue(String provider, String providerId);
    List<User> findAllByAdminTrueAndActiveTrue();
}
