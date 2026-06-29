package com.cfg.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TransferOrderRequest {
    @NotNull(message = "Target table is required")
    private UUID targetTableId;
}
