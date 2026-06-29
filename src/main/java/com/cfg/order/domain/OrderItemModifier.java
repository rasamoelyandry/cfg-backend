package com.cfg.order.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_item_modifiers")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderItemModifier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_item_id", nullable = false)
    private UUID orderItemId;

    @Column(name = "modifier_name", nullable = false, length = 100)
    private String modifierName;

    @Builder.Default
    @Column(name = "price_delta", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceDelta = BigDecimal.ZERO;
}
