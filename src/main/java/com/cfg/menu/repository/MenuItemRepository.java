package com.cfg.menu.repository;

import com.cfg.menu.domain.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    List<MenuItem> findAllByCategoryIdAndIsAvailableTrueOrderBySortOrder(UUID categoryId);
    List<MenuItem> findAllByRestaurantIdOrderByCategoryIdAscSortOrderAsc(UUID restaurantId);
    List<MenuItem> findAllByRestaurantIdAndIsAvailableTrueOrderByCategoryIdAscSortOrderAsc(UUID restaurantId);

    /**
     * Decremente le stock de facon atomique. Ne s'applique qu'aux articles avec trackStock=true
     * et suffisamment de stock disponible. Retourne 0 si l'article n'est pas suivi ou si le stock est insuffisant.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE MenuItem m SET m.stockQuantity = m.stockQuantity - :qty, " +
           "m.isAvailable = (m.stockQuantity - :qty) > 0 " +
           "WHERE m.id = :id AND m.trackStock = true AND m.stockQuantity >= :qty")
    int decrementStock(@Param("id") UUID id, @Param("qty") int qty);

    /**
     * Restitue du stock (retrait d'article / annulation de commande). No-op si l'article n'est pas suivi.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE MenuItem m SET m.stockQuantity = m.stockQuantity + :qty, " +
           "m.isAvailable = (m.stockQuantity + :qty) > 0 " +
           "WHERE m.id = :id AND m.trackStock = true")
    void restoreStock(@Param("id") UUID id, @Param("qty") int qty);

    /**
     * Reapprovisionnement manuel depuis l'admin.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE MenuItem m SET m.stockQuantity = m.stockQuantity + :qty, " +
           "m.isAvailable = (m.stockQuantity + :qty) > 0 " +
           "WHERE m.id = :id")
    void addStock(@Param("id") UUID id, @Param("qty") int qty);
}
