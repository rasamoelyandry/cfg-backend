package com.cfg.menu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CreateMenuItemRequest {

    @NotNull(message = "Category is required")
    private UUID categoryId;

    @NotBlank(message = "Item name is required")
    @Size(max = 255)
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be >= 0")
    private BigDecimal price;

    private String imageUrl;

    private int sortOrder = 0;

    private List<ModifierRequest> modifiers;

    @Data
    public static class ModifierRequest {
        @NotBlank
        private String name;
        private BigDecimal priceDelta = BigDecimal.ZERO;
        private boolean isDefault = false;
    }
}
