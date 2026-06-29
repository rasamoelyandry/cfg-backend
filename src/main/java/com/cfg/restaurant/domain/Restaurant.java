package com.cfg.restaurant.domain;

import com.cfg.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurants")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Restaurant extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 30)
    private String phone;

    private String email;

    @Builder.Default
    @Column(nullable = false, length = 10)
    private String currency = "MGA";

    @Builder.Default
    @Column(nullable = false, length = 50)
    private String timezone = "Indian/Antananarivo";

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true;
}
