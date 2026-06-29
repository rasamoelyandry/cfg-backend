package com.cfg.menu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UpdateMenuItemRequest {

    private UUID categoryId;

    @Size(max = 255)
    private String name;

    private String description;

    @DecimalMin(value = "0.00")
    private BigDecimal price;

    private String imageUrl;

    private Integer sortOrder;

    private Boolean isAvailable;
}
