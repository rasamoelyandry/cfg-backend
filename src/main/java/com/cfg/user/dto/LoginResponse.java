package com.cfg.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data @Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private UserDto user;

    @Data @Builder
    public static class UserDto {
        private UUID id;
        private UUID restaurantId;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String role;
    }
}
