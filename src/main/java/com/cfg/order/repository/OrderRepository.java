package com.cfg.order.repository;

import com.cfg.order.domain.Order;
import com.cfg.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findAllByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.restaurantId = :restaurantId AND o.status NOT IN ('PAID','CANCELLED') ORDER BY o.createdAt ASC")
    List<Order> findActiveOrdersByRestaurant(UUID restaurantId);

    List<Order> findAllByRestaurantIdAndStatus(UUID restaurantId, OrderStatus status);

    Optional<Order> findByTableIdAndStatusNotIn(UUID tableId, List<OrderStatus> statuses);

    Optional<Order> findByClientUuid(UUID clientUuid);
}
