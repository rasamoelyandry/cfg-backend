package com.cfg.menu.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCategoryRequest {

    @Size(max = 100)
    private String name;

    private String description;

    private Integer sortOrder;

    private Boolean isActive;
}
