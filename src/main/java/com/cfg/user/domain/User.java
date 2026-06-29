package com.cfg.user.domain;

import com.cfg.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class User extends BaseEntity {

    @Column(name = "restaurant_id")
    private UUID restaurantId;

    @Column(unique = true)
    private String email;

    @Column(unique = true, length = 30)
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
