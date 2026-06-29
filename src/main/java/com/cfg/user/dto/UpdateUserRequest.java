package com.cfg.user.dto;

import com.cfg.user.domain.Role;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 255)
    private String email;

    @Size(max = 30)
    private String phone;

    private String password;

    private Boolean isActive;
}
