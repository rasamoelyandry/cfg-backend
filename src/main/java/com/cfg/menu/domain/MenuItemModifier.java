package com.cfg.menu.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "menu_item_modifiers")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class MenuItemModifier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(nullable = false, length = 100)
    private String name;

    @Builder.Default
    @Column(name = "price_delta", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceDelta = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;
}
