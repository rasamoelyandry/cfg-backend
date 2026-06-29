package com.cfg.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String phone;

    @NotBlank(message = "Password is required")
    private String password;
}
