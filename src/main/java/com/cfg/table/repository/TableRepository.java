package com.cfg.table.repository;

import com.cfg.table.domain.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TableRepository extends JpaRepository<RestaurantTable, UUID> {
    List<RestaurantTable> findAllByRestaurantIdAndIsActiveTrueOrderByNumber(UUID restaurantId);
    Optional<RestaurantTable> findByRestaurantIdAndNumber(UUID restaurantId, int number);
    boolean existsByRestaurantIdAndNumber(UUID restaurantId, int number);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE RestaurantTable t SET t.occupied = true WHERE t.id = :id")
    void markOccupied(@Param("id") UUID id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE RestaurantTable t SET t.occupied = false WHERE t.id = :id")
    void markFree(@Param("id") UUID id);
}
