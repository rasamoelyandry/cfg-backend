package com.cfg.menu.repository;

import com.cfg.menu.domain.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, UUID> {
    List<MenuCategory> findAllByRestaurantIdAndIsActiveTrueOrderBySortOrder(UUID restaurantId);
}
