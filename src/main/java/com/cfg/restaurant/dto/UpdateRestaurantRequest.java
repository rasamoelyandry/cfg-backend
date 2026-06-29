package com.cfg.restaurant.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateRestaurantRequest {

    @Size(max = 255)
    private String name;

    private String address;

    @Size(max = 30)
    private String phone;

    @Size(max = 255)
    private String email;

    @Size(max = 10)
    private String currency;

    @Size(max = 50)
    private String timezone;

    private Boolean isActive;
}
