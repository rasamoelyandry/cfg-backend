package com.cfg.user.repository;

import com.cfg.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmailOrPhone(String email, String phone);
    List<User> findAllByRestaurantIdAndIsActiveTrue(UUID restaurantId);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
