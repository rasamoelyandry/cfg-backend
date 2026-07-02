package com.cfg.user.dto;

import com.cfg.user.domain.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class UserResponse {
    private UUID id;
    private UUID restaurantId;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String role;
    @JsonProperty("isActive")
    private boolean isActive;
    private Instant lastLoginAt;
    private Instant createdAt;

    public static UserResponse from(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .restaurantId(u.getRestaurantId())
                .email(u.getEmail())
                .phone(u.getPhone())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .role(u.getRole().name())
                .isActive(u.isActive())
                .lastLoginAt(u.getLastLoginAt())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
