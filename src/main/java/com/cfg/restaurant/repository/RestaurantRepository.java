package com.cfg.restaurant.repository;

import com.cfg.restaurant.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {
    Optional<Restaurant> findBySlug(String slug);
    List<Restaurant> findAllByIsActiveTrue();
    boolean existsBySlug(String slug);
}
