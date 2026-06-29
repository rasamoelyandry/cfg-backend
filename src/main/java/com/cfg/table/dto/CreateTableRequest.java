package com.cfg.table.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTableRequest {

    @NotNull(message = "Table number is required")
    @Min(value = 1, message = "Table number must be >= 1")
    private Integer number;

    private String label;

    @Min(value = 1)
    private int capacity = 4;
}
