package com.cfg.table.dto;

import com.cfg.table.domain.RestaurantTable;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TableResponse {
    private UUID id;
    private UUID restaurantId;
    private int number;
    private String label;
    private int capacity;
    @JsonProperty("isActive")
    private boolean isActive;

    public static TableResponse from(RestaurantTable t) {
        return TableResponse.builder()
                .id(t.getId())
                .restaurantId(t.getRestaurantId())
                .number(t.getNumber())
                .label(t.getLabel())
                .capacity(t.getCapacity())
                .isActive(t.isActive())
                .build();
    }
}
