package com.cfg.table.repository;

import com.cfg.table.domain.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TableRepository extends JpaRepository<RestaurantTable, UUID> {
    List<RestaurantTable> findAllByRestaurantIdAndIsActiveTrueOrderByNumber(UUID restaurantId);
    Optional<RestaurantTable> findByRestaurantIdAndNumber(UUID restaurantId, int number);
    boolean existsByRestaurantIdAndNumber(UUID restaurantId, int number);
}
