package com.cfg.menu.domain;

import com.cfg.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "menu_items")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class MenuItem extends BaseEntity {

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Builder.Default
    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = true;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "menu_item_id")
    @Builder.Default
    private List<MenuItemModifier> modifiers = new ArrayList<>();
}
