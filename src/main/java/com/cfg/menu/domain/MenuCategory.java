package com.cfg.menu.domain;

import com.cfg.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "menu_categories")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class MenuCategory extends BaseEntity {

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true;
}
