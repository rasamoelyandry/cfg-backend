package com.cfg.table.domain;

import com.cfg.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "restaurant_tables",
       uniqueConstraints = @UniqueConstraint(columnNames = {"restaurant_id", "number"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RestaurantTable extends BaseEntity {

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(nullable = false)
    private int number;

    @Column(length = 50)
    private String label;

    @Column(nullable = false)
    private int capacity = 4;

    @Column(nullable = false)
    private boolean isActive = true;
}
