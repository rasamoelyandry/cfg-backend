package com.cfg.kitchen.websocket;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class OrderEventMessage {
    private String eventType; // ORDER_CREATED, ORDER_STATUS_CHANGED, ORDER_ITEM_ADDED
    private UUID orderId;
    private UUID restaurantId;
    private UUID tableId;
    private Integer tableNumber;
    private String status;
    private String customerName;
    private BigDecimal totalAmount;
    private Instant timestamp;
    private List<ItemDto> items;

    @Data @Builder
    public static class ItemDto {
        private UUID id;
        private String menuItemName;
        private int quantity;
        private String notes;
        private String status;
    }
}
