package com.cfg.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRestaurantRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 100)
    private String slug;

    private String address;

    @Size(max = 30)
    private String phone;

    @Size(max = 255)
    private String email;

    @Size(max = 10)
    private String currency = "MGA";

    @Size(max = 50)
    private String timezone = "Indian/Antananarivo";
}
