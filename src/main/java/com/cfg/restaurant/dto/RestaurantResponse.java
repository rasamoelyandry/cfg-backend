package com.cfg.restaurant.dto;

import com.cfg.restaurant.domain.Restaurant;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class RestaurantResponse {
    private UUID id;
    private String name;
    private String slug;
    private String address;
    private String phone;
    private String email;
    private String currency;
    private String timezone;
    @JsonProperty("isActive")
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    public static RestaurantResponse from(Restaurant r) {
        return RestaurantResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .slug(r.getSlug())
                .address(r.getAddress())
                .phone(r.getPhone())
                .email(r.getEmail())
                .currency(r.getCurrency())
                .timezone(r.getTimezone())
                .isActive(r.isActive())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
