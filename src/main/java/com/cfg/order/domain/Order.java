package com.cfg.order.domain;

import com.cfg.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Order extends BaseEntity {

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "table_id")
    private UUID tableId;

    @Column(name = "waiter_id")
    private UUID waiterId;

    @Column(name = "customer_name", length = 255)
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private OrderStatus status = OrderStatus.DRAFT;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "client_uuid", unique = true)
    private UUID clientUuid;

    @Column(name = "sent_to_kitchen_at")
    private Instant sentToKitchenAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    public void recalculateTotal() {
        this.totalAmount = items.stream()
                .filter(item -> item.getStatus() != OrderItemStatus.CANCELLED)
                .map(item -> item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()))
                        .add(item.getModifiers().stream()
                                .map(m -> m.getPriceDelta())
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .multiply(BigDecimal.valueOf(item.getQuantity()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
