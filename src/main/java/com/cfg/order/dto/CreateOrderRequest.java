package com.cfg.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateOrderRequest {
    private UUID tableId;
    private String customerName;
    private String notes;
    private UUID clientUuid;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private UUID menuItemId;
        private int quantity = 1;
        private String notes;
        private List<UUID> modifierIds;
    }
}
