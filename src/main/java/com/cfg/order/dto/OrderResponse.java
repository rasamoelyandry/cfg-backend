package com.cfg.order.dto;

import com.cfg.order.domain.Order;
import com.cfg.order.domain.OrderItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data @Builder
public class OrderResponse {
    private UUID id;
    private UUID restaurantId;
    private UUID tableId;
    private UUID waiterId;
    private String customerName;
    private String status;
    private String notes;
    private BigDecimal totalAmount;
    private UUID clientUuid;
    private Instant sentToKitchenAt;
    private Instant createdAt;
    private Instant updatedAt;
    private List<OrderItemResponse> items;

    @Data @Builder
    public static class OrderItemResponse {
        private UUID id;
        private UUID menuItemId;
        private String menuItemName;
        private BigDecimal unitPrice;
        private int quantity;
        private String notes;
        private String status;
    }

    public static OrderResponse from(Order o) {
        return OrderResponse.builder()
                .id(o.getId())
                .restaurantId(o.getRestaurantId())
                .tableId(o.getTableId())
                .waiterId(o.getWaiterId())
                .customerName(o.getCustomerName())
                .status(o.getStatus().name())
                .notes(o.getNotes())
                .totalAmount(o.getTotalAmount())
                .clientUuid(o.getClientUuid())
                .sentToKitchenAt(o.getSentToKitchenAt())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .items(o.getItems().stream().map(OrderResponse::itemFrom).collect(Collectors.toList()))
                .build();
    }

    private static OrderItemResponse itemFrom(OrderItem i) {
        return OrderItemResponse.builder()
                .id(i.getId())
                .menuItemId(i.getMenuItemId())
                .menuItemName(i.getMenuItemName())
                .unitPrice(i.getUnitPrice())
                .quantity(i.getQuantity())
                .notes(i.getNotes())
                .status(i.getStatus().name())
                .build();
    }
}
