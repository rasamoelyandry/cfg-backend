package com.cfg.table.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateTableRequest {
    private String label;

    @Min(value = 1)
    private Integer capacity;

    private Boolean isActive;
}
