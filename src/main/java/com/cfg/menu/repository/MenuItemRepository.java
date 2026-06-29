package com.cfg.menu.repository;

import com.cfg.menu.domain.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    List<MenuItem> findAllByCategoryIdAndIsAvailableTrueOrderBySortOrder(UUID categoryId);
    List<MenuItem> findAllByRestaurantIdOrderByCategoryIdAscSortOrderAsc(UUID restaurantId);
    List<MenuItem> findAllByRestaurantIdAndIsAvailableTrueOrderByCategoryIdAscSortOrderAsc(UUID restaurantId);
}
